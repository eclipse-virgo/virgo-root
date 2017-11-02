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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Utility methods for working with Equinox internals.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class EquinoxUtils {

    /**
     * Gets the {@link ClassLoader} for the supplied {@link Bundle}.
     * 
     * @param bundle the bundle.
     * @return the bundle <code>ClassLoader</code>.
     */
    public static ClassLoader getBundleClassLoader(Bundle bundle) {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring == null) {
            throw new IllegalStateException("Unable to access BundleWiring for bundle '" + bundle.getSymbolicName() + "'.");
        }
        return wiring.getClassLoader();
    }

    /**
     * Gets all direct dependencies of the supplied {@link Bundle}.
     * 
     * @param bundle the <code>Bundle</code>.
     * @param bundleContext the {@link BundleContext} to use for service access - typically the system
     *        <code>BundleContext</code>.
     * @param serverAdmin the {@link PlatformAdmin} service.
     * @return the direct dependencies.
     */
    // TODO track down usages and remove them
    public static Bundle[] getDirectDependencies(Bundle bundle, BundleContext bundleContext, PlatformAdmin serverAdmin) {
        return getDirectDependencies(bundle, bundleContext, serverAdmin, false);
    }

    /**
     * Gets all direct dependencies of the supplied {@link Bundle}, optionally included fragments of the direct
     * dependencies in the returned array.
     * 
     * @param bundle the <code>Bundle</code>.
     * @param bundleContext the {@link BundleContext} to use for service access - typically the system
     *        <code>BundleContext</code>.
     * @param serverAdmin the {@link PlatformAdmin} service.
     * @param includeFragments whether to include fragments or no
     * @return an array of {@link Bundle}s which are direct dependencies
     */
    // TODO track down usages and remove them
    public static Bundle[] getDirectDependencies(Bundle bundle, BundleContext bundleContext, PlatformAdmin serverAdmin, boolean includeFragments) {
        State state = serverAdmin.getState(false);

        ExportPackageDescription[] exportPackageDescriptions = serverAdmin.getStateHelper().getVisiblePackages(state.getBundle(bundle.getBundleId()));

        Set<Bundle> dependencies = new HashSet<Bundle>();

        for (ExportPackageDescription exportPackageDescription : exportPackageDescriptions) {
            BundleDescription bundleDescription = exportPackageDescription.getExporter();
            if (bundleDescription.getBundleId() != bundle.getBundleId()) {
                Bundle dependencyBundle = bundleContext.getBundle(bundleDescription.getBundleId());
                // Handle an uninstalled dependent bundle gracefully.
                if (dependencyBundle != null) {
                    dependencies.add(dependencyBundle);
                    if (includeFragments) {
                        BundleDescription[] fragmentDescriptions = bundleDescription.getFragments();
                        for (BundleDescription fragmentDescription : fragmentDescriptions) {
                            Bundle fragment = bundleContext.getBundle(fragmentDescription.getBundleId());
                            if (fragment != null) {
                                dependencies.add(fragment);
                            }
                        }
                    }
                }
            }
        }

        return dependencies.toArray(new Bundle[dependencies.size()]);
    }

    // TODO add JavaDoc
    // TODO handle the includeFragments
    public static Bundle[] getDirectDependencies(Bundle bundle, boolean includeFragments) {
        Set<Bundle> dependencies = getDependencies(bundle);
        return dependencies.toArray(new Bundle[dependencies.size()]);
    }

    private static Set<Bundle> getDependencies(Bundle bundle) {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        Set<Bundle> dependencies = new HashSet<Bundle>();
        // first get the imported packages
        List<BundleWire> packageWires = wiring.getRequiredWires(PackageNamespace.PACKAGE_NAMESPACE);
        for (BundleWire packageWire : packageWires) {
            dependencies.add(packageWire.getProvider().getBundle());
        }
        // now get dependencies from required bundles
        for (BundleWire requiredWire : wiring.getRequiredWires(BundleNamespace.BUNDLE_NAMESPACE)) {
            getRequiredBundleDependencies(requiredWire, dependencies);
        }
        return dependencies;
    }

    private static void getRequiredBundleDependencies(BundleWire requiredWire, Set<Bundle> dependencies) {
        BundleWiring providerWiring = requiredWire.getProviderWiring();
        dependencies.add(providerWiring.getBundle());
        // now get re-exported requires of the required bundle
        for (BundleWire providerBundleWire : providerWiring.getRequiredWires(BundleNamespace.BUNDLE_NAMESPACE)) {
            String visibilityDirective = providerBundleWire.getRequirement().getDirectives().get(BundleNamespace.REQUIREMENT_VISIBILITY_DIRECTIVE);
            if (BundleNamespace.VISIBILITY_REEXPORT.equals(visibilityDirective)) {
                getRequiredBundleDependencies(providerBundleWire, dependencies);
            }
        }
    }

    /**
     * Queries whether the supplied {@link Bundle} is the system bundle.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return <code>true</code> if <code>bundle</code> is the system bundle, otherwise <code>false</code>.
     */
    public static boolean isSystemBundle(Bundle bundle) {
        return bundle != null && bundle.getBundleId() == 0;
    }
}
