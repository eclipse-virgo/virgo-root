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

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.loader.BundleLoader;
import org.eclipse.osgi.internal.loader.EquinoxClassLoader;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo.Generation;
import org.eclipse.osgi.storage.bundlefile.BundleEntry;
import org.eclipse.virgo.kernel.osgi.framework.ExtendedClassNotFoundException;
import org.eclipse.virgo.kernel.osgi.framework.ExtendedNoClassDefFoundError;
import org.eclipse.virgo.kernel.osgi.framework.InstrumentableClassLoader;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension to {@link DefaultClassLoader} that adds instrumentation support.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * As threadsafe as <code>DefaultClassLoader</code>.
 * 
 */
public final class KernelBundleClassLoader extends EquinoxClassLoader implements InstrumentableClassLoader {

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

    private final List<ClassFileTransformer> classFileTransformers = new CopyOnWriteArrayList<ClassFileTransformer>();

    private final String[] instrumentedPackages;

    private final String bundleScope;

    private final Set<Class<Driver>> loadedDriverClasses = new HashSet<Class<Driver>>();

    private final Object monitor = new Object();

    private volatile boolean instrumented;

    // TODO fix JavaDoc once the solution is settled
    /**
     * Constructs a new <code>ServerBundleClassLoader</code>.
     * 
     * @param parent the parent <code>ClassLoader</code>.
     * @param delegate the delegate for this ClassLoader</code>
     * @param bundledata the bundledata for this ClassLoader</code>
     */
    // TODO change order in constructor to match EquinoxClassLoader
    KernelBundleClassLoader(ClassLoader parent, BundleLoader delegate, EquinoxConfiguration configuration, Generation generation) {
        super(parent, configuration, delegate, generation);
        this.bundleScope = OsgiFrameworkUtils.getScopeName(generation);
        this.instrumentedPackages = findInstrumentedPackages(generation);
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

    /**
     * @param bundle
     * @return
     */
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
    private String[] findInstrumentedPackages(Generation generation) {
        String headerValue = (String) generation.getHeaders().get(HEADER_INSTRUMENT_PACKAGE);
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
        return !EquinoxUtils.isSystemBundle(bundle) && this.bundleScope != null && this.bundleScope.equals(OsgiFrameworkUtils.getScopeName(bundle));
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
        final ClasspathManager manager = new ClasspathManager(getGeneration(), this);
        // TODO check if we need to initialize the new ClasspathManager
//        manager.initialize();
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
    public DefineClassResult defineClass(String name, byte[] classbytes, ClasspathEntry classpathEntry) {
        byte[] transformedBytes = classbytes;
        if (shouldInstrument(name)) {
            for (ClassFileTransformer transformer : this.classFileTransformers) {
                try {
                    String transformName = name.replaceAll("\\.", "/");
                    byte[] transform = transformer.transform(this, transformName, null, getGeneration().getDomain(), transformedBytes);
                    if (transform != null) {
                        transformedBytes = transform;
                    }
                } catch (IllegalClassFormatException e) {
                    // TODO how to get bundle name from Generation?!
                    throw new ClassFormatError("Error reading class from bundle entry '" + getGeneration().getBundleInfo().getBundleId() + "'. " + e.getMessage());
                }
            }
        }
        try {
            // TODO why is entry not needed anymore?
            DefineClassResult definedClass = super.defineClass(name, transformedBytes, classpathEntry);
            // TODO store only if defined?!
            if (definedClass.defined) {
                storeClassIfDriver(definedClass.clazz);
            }
            // TODO throw ExtendedNoClassDefFoundError if defined == false?
            return definedClass;
        } catch (NoClassDefFoundError e) {
            // TODO - think about using DefineClassResult to handle this case
            throw new ExtendedNoClassDefFoundError(this, e);
        }
    }

    /**
     * @param definedClass
     */
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
            localLoadedDriverClasses = new HashSet<Class<Driver>>(this.loadedDriverClasses);
        }

        synchronized (DriverManager.class) {
            // TODO remove Java 6 support ?!
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
                    List<Object> driverElementsToRemove = new ArrayList<Object>();

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
        // TODO - how to get the delegate?!
        Bundle bundle = getGeneration().getRevision().getBundle();
//        bundle.adapt(BundleWiring.class).getClassLoader();
        return String.format("%s: [bundle=%s]", getClass().getSimpleName(), bundle);
    }

    private Bundle[] getDependencyBundles(boolean includeDependenciesFragments) {
        Bundle bundle = getGeneration().getRevision().getBundle();
        Bundle[] deps = EquinoxUtils.getDirectDependencies(bundle, includeDependenciesFragments);
        return deps;
    }

    /**
     * Gets the {@link BundleContext} for this ClassLoader's {@link Bundle}.
     * 
     * @return the <code>BundleContext</code>.
     */
//    private BundleContext getBundleContext() {
//        return this.manager.getBaseData().getAdaptor().getContext();
//    }

    /**
     * Gets the {@link PlatformAdmin} service.
     * 
     * @return the <code>PlatformAdmin</code> service.
     */
//    private PlatformAdmin getPlatformAdmin() {
//        return this.manager.getBaseData().getAdaptor().getPlatformAdmin();
//    }

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

        private final ConcurrentMap<String, Class<?>> loadedClasses = new ConcurrentHashMap<String, Class<?>>();

        private final ClasspathManager manager;

        /**
         * @param manager
         */
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
