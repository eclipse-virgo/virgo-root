/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.virgo.util.osgi.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.osgi.framework.ExtendedClassNotFoundException;
import org.eclipse.virgo.kernel.osgi.framework.ExtendedNoClassDefFoundError;
import org.eclipse.virgo.kernel.osgi.framework.InstrumentableClassLoader;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;

/**
 * Extension to {@link DefaultClassLoader} that adds instrumentation support.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * As threadsafe as <code>DefaultClassLoader</code>.
 * 
 */
public final class KernelBundleClassLoader extends DefaultClassLoader implements InstrumentableClassLoader {

    static {
        try {
            Method parallelCapableMethod = ClassLoader.class.getDeclaredMethod("registerAsParallelCapable", (Class[]) null);
            parallelCapableMethod.setAccessible(true);
            parallelCapableMethod.invoke(null, new Object[0]);
        } catch (Throwable e) {
            // must avoid failing in clinit
        }
    }

    private static final String[] EXCLUDED_PACKAGES = new String[] { "java.", "javax.", "sun.", "oracle." };

    private static final String HEADER_INSTRUMENT_PACKAGE = "Instrument-Package";

    private static final Logger LOGGER = LoggerFactory.getLogger(KernelBundleClassLoader.class);

    private final List<ClassFileTransformer> classFileTransformers = new CopyOnWriteArrayList<>();

    private final String[] instrumentedPackages;

    private final String[] classpath;

    private final String bundleScope;

    private final Set<Class<Driver>> loadedDriverClasses = new HashSet<>();

    private final Object monitor = new Object();

    private volatile boolean instrumented;

    /**
     * Constructs a new <code>ServerBundleClassLoader</code>.
     * 
     * @param parent the parent <code>ClassLoader</code>.
     * @param delegate the delegate for this ClassLoader</code>
     * @param domain the domain for this ClassLoader</code>
     * @param bundledata the bundledata for this ClassLoader</code>
     * @param classpath the classpath for this ClassLoader</code>
     */
    KernelBundleClassLoader(ClassLoader parent, ClassLoaderDelegate delegate, ProtectionDomain domain, BaseData bundledata, String[] classpath) {
        super(parent, delegate, domain, bundledata, classpath);
        this.classpath = classpath;
        this.bundleScope = OsgiFrameworkUtils.getScopeName(bundledata.getBundle());
        this.instrumentedPackages = findInstrumentedPackages(bundledata.getBundle());
    }

    /**
     * {@inheritDoc}
     */
    public void addClassFileTransformer(ClassFileTransformer transformer) {
        this.instrumented = true;
        synchronized (this.classFileTransformers) {
            if (this.classFileTransformers.contains(transformer)) {
                return;
            }
            this.classFileTransformers.add(transformer);
        }
        Bundle[] bundles = getDependencyBundles(false);
        for (Bundle bundle : bundles) {
            if (propagateInstrumentationTo(bundle)) {
                ClassLoader bundleClassLoader = getBundleClassLoader(bundle);
                if (bundleClassLoader instanceof KernelBundleClassLoader) {
                    ((KernelBundleClassLoader) bundleClassLoader).addClassFileTransformer(transformer);
                }
            }
        }
    }

    private ClassLoader getBundleClassLoader(Bundle bundle) {
        return EquinoxUtils.getBundleClassLoader(bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            Class<?> loadedClass = super.loadClass(name, resolve);
            storeClassIfDriver(loadedClass);
            return loadedClass;
        } catch (ClassNotFoundException e) {
            throw new ExtendedClassNotFoundException(this, e);
        } catch (NoClassDefFoundError e) {
            throw new ExtendedNoClassDefFoundError(this, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInstrumented() {
        return this.instrumented;
    }

    /**
     * {@inheritDoc}
     */
    public int getClassFileTransformerCount() {
        return this.classFileTransformers.size();
    }

    /**
     * Finds the explicit list of packages to include in instrumentation (if specified).
     */
    private String[] findInstrumentedPackages(Bundle bundle) {
        String headerValue = bundle.getHeaders().get(HEADER_INSTRUMENT_PACKAGE);
        if (headerValue == null || headerValue.length() == 0) {
            return new String[0];
        } else {
            String[] vals = headerValue.split(",");
            String[] packageNames = new String[vals.length];
            for (int x = 0; x < packageNames.length; x++) {
                packageNames[x] = vals[x].trim();
            }
            return packageNames;
        }
    }

    private boolean propagateInstrumentationTo(Bundle bundle) {
        return !BundleUtils.isSystemBundle(bundle) && this.bundleScope != null && this.bundleScope.equals(OsgiFrameworkUtils.getScopeName(bundle));
    }

    private boolean shouldInstrument(String className) {
        return includedForInstrumentation(className) && !excludedFromInstrumentation(className);
    }

    private boolean includedForInstrumentation(String className) {
        if (this.instrumentedPackages.length == 0) {
            return true;
        }
        for (String instrumentedPackage : this.instrumentedPackages) {
            if (className.startsWith(instrumentedPackage)) {
                return true;
            }
        }
        return false;
    }

    private boolean excludedFromInstrumentation(String className) {
        for (String excludedPackage : EXCLUDED_PACKAGES) {
            if (className.startsWith(excludedPackage)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public ThrowAwayClassLoader createThrowAway() {
        final ClasspathManager manager = new ClasspathManager(this.manager.getBaseData(), this.classpath, this);
        manager.initialize();
        return AccessController.doPrivileged(new PrivilegedAction<ThrowAwayClassLoader>() {

            public ThrowAwayClassLoader run() {
                return new ThrowAwayClassLoader(manager);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> defineClass(String name, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry) {

        byte[] transformedBytes = classbytes;
        if (shouldInstrument(name)) {
            for (ClassFileTransformer transformer : this.classFileTransformers) {
                try {
                    String transformName = name.replaceAll("\\.", "/");
                    byte[] transform = transformer.transform(this, transformName, null, this.domain, transformedBytes);
                    if (transform != null) {
                        transformedBytes = transform;
                    }
                } catch (IllegalClassFormatException e) {
                    throw new ClassFormatError("Error reading class from bundle entry '" + entry.getName() + "'. " + e.getMessage());
                }
            }
        }
        try {
            Class<?> definedClass = super.defineClass(name, transformedBytes, classpathEntry, entry);
            storeClassIfDriver(definedClass);
            return definedClass;
        } catch (NoClassDefFoundError e) {
            throw new ExtendedNoClassDefFoundError(this, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void storeClassIfDriver(Class<?> candidateClass) {
        if (Driver.class.isAssignableFrom(candidateClass)) {
            synchronized (this.monitor) {
                this.loadedDriverClasses.add((Class<Driver>) candidateClass);
            }
        }
    }

    @Override
    public void close() {
        clearJdbcDrivers();
    }

    private void clearJdbcDrivers() {
        Set<Class<Driver>> localLoadedDriverClasses;
        synchronized (this.monitor) {
            localLoadedDriverClasses = new HashSet<>(this.loadedDriverClasses);
        }

        synchronized (DriverManager.class) {
            try { // Java 6
                Field writeDriversField = DriverManager.class.getDeclaredField("writeDrivers");
                writeDriversField.setAccessible(true);
                Vector<?> writeDrivers = (Vector<?>) writeDriversField.get(null);

                Iterator<?> driverElements = writeDrivers.iterator();

                while (driverElements.hasNext()) {
                    Object driverObj = driverElements.next();
                    Field driverField = driverObj.getClass().getDeclaredField("driver");
                    driverField.setAccessible(true);
                    if (localLoadedDriverClasses.contains(driverField.get(driverObj).getClass())) {
                        driverElements.remove();
                    }
                }

                Vector<?> readDrivers = (Vector<?>) writeDrivers.clone();
                Field readDriversField = DriverManager.class.getDeclaredField("readDrivers");
                readDriversField.setAccessible(true);
                readDriversField.set(null, readDrivers);
                LOGGER.debug("Cleared JDBC drivers for " + this + " using Java 6 strategy");
            } catch (Exception javaSixWayFailed) {
                try { // Java 7
                    Field registeredDriversField = DriverManager.class.getDeclaredField("registeredDrivers");
                    registeredDriversField.setAccessible(true);
                    CopyOnWriteArrayList<?> registeredDrivers = (CopyOnWriteArrayList<?>) registeredDriversField.get(null);

                    Iterator<?> driverElements = registeredDrivers.iterator();
                    List<Object> driverElementsToRemove = new ArrayList<>();

                    while (driverElements.hasNext()) {
                        Object driverObj = driverElements.next();
                        Field driverField = driverObj.getClass().getDeclaredField("driver");
                        driverField.setAccessible(true);
                        if (localLoadedDriverClasses.contains(driverField.get(driverObj).getClass())) {
                            driverElementsToRemove.add(driverObj);
                        }
                    }

                    for (Object driverObj : driverElementsToRemove) {
                        registeredDrivers.remove(driverObj);
                    }
                    LOGGER.debug("Cleared JDBC drivers for " + this + " using Java 7 strategy");
                } catch (Exception e) {
                    LOGGER.warn("Failure when clearing JDBC drivers for " + this, e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s: [bundle=%s]", getClass().getSimpleName(), this.delegate);
    }

    private Bundle[] getDependencyBundles(boolean includeDependenciesFragments) {
        Bundle bundle = this.manager.getBaseData().getBundle();
        BundleContext systemBundleContext = getBundleContext();
        PlatformAdmin serverAdmin = getPlatformAdmin();
        return EquinoxUtils.getDirectDependencies(bundle, systemBundleContext, serverAdmin, includeDependenciesFragments);
    }

    /**
     * Gets the {@link BundleContext} for this ClassLoader's {@link Bundle}.
     * 
     * @return the <code>BundleContext</code>.
     */
    private BundleContext getBundleContext() {
        return this.manager.getBaseData().getAdaptor().getContext();
    }

    /**
     * Gets the {@link PlatformAdmin} service.
     * 
     * @return the <code>PlatformAdmin</code> service.
     */
    private PlatformAdmin getPlatformAdmin() {
        return this.manager.getBaseData().getAdaptor().getPlatformAdmin();
    }

    /**
     * Throwaway classloader for OSGi bundles.
     * <p/>
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * As threadsafe as {@link ClassLoader}.
     * 
     */
    final class ThrowAwayClassLoader extends ClassLoader {

        private final ConcurrentMap<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

        private final ClasspathManager manager;

        private ThrowAwayClassLoader(ClasspathManager manager) {
            this.manager = manager;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!shouldInstrument(name)) {
                return KernelBundleClassLoader.this.loadClass(name, resolve);
            }
            Class<?> cls = KernelBundleClassLoader.this.findLoadedClass(name);
            if (cls == null) {
                cls = this.loadedClasses.get(name);
                if (cls == null) {
                    cls = findClassInternal(name, true);
                    if (cls == null) {
                        cls = KernelBundleClassLoader.this.loadClass(name, resolve);
                    }
                }
            }
            if (cls == null) {
                throw new ClassNotFoundException(name);
            }
            if (resolve) {
                resolveClass(cls);
            }
            // TODO review findbugs warning vs. failing tests in LoadTimeWeavingTests
            this.loadedClasses.putIfAbsent(name, cls);
            return cls;
        }

        /**
         * Attempts to find a <code>Class</code> from the enclosing bundle.
         * 
         * @param name the name of the <code>Class</code>
         * @param traverseDependencies should dependency bundles be checked for the class.
         */
        Class<?> findClassInternal(String name, boolean traverseDependencies) {
            String path = name.replaceAll("\\.", "/").concat(".class");

            BundleEntry entry = this.manager.findLocalEntry(path);
            if (entry == null) {
                if (traverseDependencies) {
                    return findClassFromImport(name);
                } else {
                    return null;
                }
            }
            byte[] bytes;
            try {
                bytes = entry.getBytes();
            } catch (IOException e) {
                bytes = null;
            }
            return bytes == null ? null : defineClass(name, bytes, 0, bytes.length);
        }

        /**
         * Attempts to locate a <code>Class</code> from one of the imported bundles.
         * 
         * @param name the <code>Class</code> name.
         * @return the located <code>Class</code>, or <code>null</code> if no <code>Class</code> can be found.
         */
        private Class<?> findClassFromImport(String name) {
            Bundle[] deps = getDependencyBundles(false);
            for (Bundle dep : deps) {
                ClassLoader depClassLoader = getBundleClassLoader(dep);
                if (depClassLoader instanceof KernelBundleClassLoader) {
                    KernelBundleClassLoader pbcl = (KernelBundleClassLoader) depClassLoader;
                    Class<?> loadedClass = pbcl.publicFindLoaded(name);
                    if (loadedClass != null) {
                        return loadedClass;
                    }
                    ThrowAwayClassLoader throwAway = pbcl.createThrowAway();
                    Class<?> cls = throwAway.findClassInternal(name, false);
                    if (cls != null) {
                        return cls;
                    }
                }

            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public URL getResource(String name) {
            return KernelBundleClassLoader.this.getResource(name);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return KernelBundleClassLoader.this.getResources(name);
        }
    }
}
