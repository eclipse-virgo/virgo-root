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

package org.eclipse.virgo.kernel.equinox.extensions.hooks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Extends a {@link ClassLoaderHook} which in {@link #postFindResource} and {@link #postFindResources} propagates the
 * attempt to get <code>META-INF</code> resource(s) to the principle bundle's dependencies, unless the request is being
 * driven through Spring DM's DelgatedNamespaceHandlerResolver.
 * 
 * <p />
 * 
 * The list of a bundle's dependencies are cached to avoid determining the dependencies every time. A bundle's entry in
 * the cached is cleared whenever an <code>UNRESOLVED</code> event is received for the bundle. <code>UNRESOLVED</code>
 * events are fired both during uninstall and during {@link PackageAdmin#refreshPackages(Bundle[]) refreshPackages}
 * processing.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
// TODO rename to ClassLoaderHook
public class MetaInfResourceClassLoaderDelegateHook extends ClassLoaderHook {

    private static final String SPRINGDM_DELEGATED_NAMESPACE_HANDLER_RESOLVER_CLASS_NAME = "org.springframework.osgi.context.support.DelegatedNamespaceHandlerResolver";

    private static final String SPRINGDM_DELEGATED_ENTITY_RESOLVER_CLASS_NAME = "org.springframework.osgi.context.support.DelegatedEntityResolver";

    private static final String BLUEPRINT_DELEGATED_NAMESPACE_HANDLER_RESOLVER_CLASS_NAME = "org.eclipse.gemini.blueprint.context.support.DelegatedNamespaceHandlerResolver";

    private static final String BLUEPRINT_DELEGATED_ENTITY_RESOLVER_CLASS_NAME = "org.eclipse.gemini.blueprint.context.support.ChainedEntityResolver";

    private static final String EXCLUDED_RESOURCE_MANIFEST = "MANIFEST.MF";

    private static final String EXCLUDED_RESOURCE_SPRING_DIR = "spring";

    private static final String EXCLUDED_RESOURCE_BLUEPRINT_DIR = "blueprint";

    private static final String EXCLUDED_RESOURCE_SPRING_DIR_SUFFIX = ".xml";

    private final BundleContext systemBundleContext;

    private final PackageAdmin packageAdmin;

    private final ThreadLocal<Object> resourceSearchInProgress = new ThreadLocal<Object>();

    private final Object SEARCH_IN_PROGRESS_MARKER = new Object();

    private final Object monitor = new Object();

    private final WeakHashMap<Bundle, Set<Bundle>> dependenciesCache = new WeakHashMap<Bundle, Set<Bundle>>();

    private final BundleListener cacheClearingBundleListener = new CacheClearingBundleListener();

    /**
     * Create a new hook that will use the supplied <code>systemBundleContext</code> to lookup bundles, and the supplied
     * <code>packageAdmin</code> to determine a bundle's dependencies.
     * 
     * @param systemBundleContext the {@link BundleContext} of the system bundle
     * @param packageAdmin the {@link PackageAdmin} to use to determine a bundle's dependencies
     */
    public MetaInfResourceClassLoaderDelegateHook(BundleContext systemBundleContext, PackageAdmin packageAdmin) {
        this.systemBundleContext = systemBundleContext;
        this.packageAdmin = packageAdmin;
    }

    public void init() {
        this.systemBundleContext.addBundleListener(this.cacheClearingBundleListener);
    }

    public void destroy() {
        this.systemBundleContext.removeBundleListener(this.cacheClearingBundleListener);
    }

    /**
     * {@inheritDoc}
     */
    public URL postFindResource(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        if (this.resourceSearchInProgress.get() == null && isDelegatedResource(name)) {
            try {
                this.resourceSearchInProgress.set(SEARCH_IN_PROGRESS_MARKER);

                Bundle[] bundles = getDependencyBundles(classLoader.getBundle());
                for (Bundle dependency : bundles) {
                    try {
                        int state = dependency.getState();
                        if (state == Bundle.ACTIVE || state == Bundle.RESOLVED) {
                            URL resource = dependency.getResource(name);
                            if (resource != null) {
                                return resource;
                            }
                        } else {
                            removeDependency(classLoader.getBundle(), dependency);
                        }
                    } catch (IllegalStateException ise) {
                        // Dependency now UNINSTALLED
                        removeDependency(classLoader.getBundle(), dependency);
                    }
                }
            } finally {
                this.resourceSearchInProgress.set(null);
            }
        }
        return null;

    }

    /**
     * {@inheritDoc}
     */
    public Enumeration<URL> postFindResources(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        if (this.resourceSearchInProgress.get() == null && isDelegatedResource(name)) {
            try {
                this.resourceSearchInProgress.set(SEARCH_IN_PROGRESS_MARKER);

                Set<URL> found = new HashSet<URL>();
                Bundle[] bundles = getDependencyBundles(classLoader.getBundle());
                for (Bundle dependency : bundles) {
                    try {
                        int state = dependency.getState();
                        if (state == Bundle.RESOLVED || state == Bundle.ACTIVE) {
                            addAll(found, dependency.getResources(name));
                        } else {
                            removeDependency(classLoader.getBundle(), dependency);
                        }
                    } catch (IOException ignored) {
                    } catch (IllegalStateException ise) {
                        // Dependency now UNINSTALLED
                        removeDependency(classLoader.getBundle(), dependency);
                    }
                }

                if (!found.isEmpty()) {
                    return new IteratorEnumerationAdaptor<URL>(found.iterator());
                }
            } finally {
                this.resourceSearchInProgress.set(null);
            }
        }

        return null;
    }

    private boolean isDelegatedResource(String name) {
        return isMetaInfResource(name) && !isDelegatedResolverCall();
    }

    /**
     * Queries whether or not the supplied resource name is a META-INF resource.
     * 
     * @param name the resource name.
     * @return <code>true</code> if the resource is a META-INF resource.
     */
    private boolean isMetaInfResource(String name) {
        if (!name.startsWith("/META-INF") && !name.startsWith("META-INF")) {
            return false;
        }
        if (name.contains(EXCLUDED_RESOURCE_MANIFEST)) {
            return false;
        }
        if ((name.contains(EXCLUDED_RESOURCE_SPRING_DIR) || name.contains(EXCLUDED_RESOURCE_BLUEPRINT_DIR))
            && name.endsWith(EXCLUDED_RESOURCE_SPRING_DIR_SUFFIX)) {
            return false;
        }
        return true;
    }

    private boolean isDelegatedResolverCall() {
        Class<?>[] stackTrace = new SecurityManagerExecutionStackAccessor().getExecutionStack();
        return isSpringDmDelegatedResolverCall(stackTrace) || isBlueprintDelegatedResolverCall(stackTrace);
    }

    private static final class SecurityManagerExecutionStackAccessor extends SecurityManager {

        public Class<?>[] getExecutionStack() {
            Class<?>[] classes = super.getClassContext();
            Class<?>[] executionStack = new Class<?>[classes.length - 1];

            System.arraycopy(classes, 1, executionStack, 0, executionStack.length);

            return executionStack;
        }
    }

    private boolean isSpringDmDelegatedResolverCall(Class<?>[] stackTrace) {
        return isDelegatedResolverCall(stackTrace, SPRINGDM_DELEGATED_NAMESPACE_HANDLER_RESOLVER_CLASS_NAME,
            SPRINGDM_DELEGATED_ENTITY_RESOLVER_CLASS_NAME);
    }

    private boolean isBlueprintDelegatedResolverCall(Class<?>[] stackTrace) {
        return isDelegatedResolverCall(stackTrace, BLUEPRINT_DELEGATED_NAMESPACE_HANDLER_RESOLVER_CLASS_NAME,
            BLUEPRINT_DELEGATED_ENTITY_RESOLVER_CLASS_NAME);
    }

    private boolean isDelegatedResolverCall(Class<?>[] stackTrace, String namespaceResolver, String entityResolver) {
        for (Class<?> clazz : stackTrace) {
            String className = clazz.getName();
            if (namespaceResolver.equals(className) || entityResolver.equals(className)) {
                return true;
            }
        }
        return false;
    }

    private void addAll(Collection<URL> target, Enumeration<URL> source) {
        while (source != null && source.hasMoreElements()) {
            target.add(source.nextElement());
        }
    }

    private Bundle[] getDependencyBundles(Bundle bundle) {
        synchronized (this.monitor) {
            Set<Bundle> dependencies = this.dependenciesCache.get(bundle);
            if (dependencies != null) {
                return dependencies.toArray(new Bundle[dependencies.size()]);
            }
        }

        Set<Bundle> dependencies = determineDependencies(bundle);
        synchronized (this.monitor) {
            this.dependenciesCache.put(bundle, dependencies);
            return dependencies.toArray(new Bundle[dependencies.size()]);
        }
    }

    private void removeDependency(Bundle bundle, Bundle dependency) {
        synchronized (this.monitor) {
            Set<Bundle> dependencies = this.dependenciesCache.get(bundle);
            if (dependencies != null) {
                dependencies.remove(dependency);
            }
        }
    }

    // TODO - rework to use EquinoxUtils.getDirectDependencies(bundle, includeFragments = false)?
    private Set<Bundle> determineDependencies(Bundle bundle) {
        Set<Bundle> bundles = new HashSet<Bundle>();
        for (Bundle candidate : this.systemBundleContext.getBundles()) {
            if (!candidate.equals(bundle)) {
                ExportedPackage[] exportedPackages = getExportedPackages(candidate);
                if (exportedPackages != null) {
                    for (ExportedPackage exportedPackage : exportedPackages) {
                        Bundle[] importingBundles = exportedPackage.getImportingBundles();
                        if (importingBundles != null) {
                            for (Bundle importer : importingBundles) {
                                if (importer.equals(bundle)) {
                                    bundles.add(candidate);
                                }
                            }
                        }
                    }
                }
            }
        }
        return bundles;
    }

    protected ExportedPackage[] getExportedPackages(Bundle bundle) {
        return this.packageAdmin.getExportedPackages(bundle);
    }

    private static class IteratorEnumerationAdaptor<T> implements Enumeration<T> {

        private final Iterator<T> iterator;

        private IteratorEnumerationAdaptor(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasMoreElements() {
            return this.iterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        public T nextElement() {
            return this.iterator.next();
        }
    }

    private final class CacheClearingBundleListener implements BundleListener {

        /**
         * {@inheritDoc}
         */
        public void bundleChanged(BundleEvent event) {
            if (BundleEvent.UNRESOLVED == event.getType()) {
                synchronized (monitor) {
                    dependenciesCache.remove(event.getBundle());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> postFindClass(String name, ModuleClassLoader classLoader) throws ClassNotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String postFindLibrary(String name, ModuleClassLoader classLoader) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> preFindClass(String name, ModuleClassLoader classLoader) throws ClassNotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String preFindLibrary(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public URL preFindResource(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Enumeration<URL> preFindResources(String name, ModuleClassLoader classLoader) throws FileNotFoundException {
        return null;
    }
}
