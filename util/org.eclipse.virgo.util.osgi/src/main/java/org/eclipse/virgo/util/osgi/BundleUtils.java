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

package org.eclipse.virgo.util.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.*;
import org.osgi.framework.BundleContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.osgi.framework.Constants.SYSTEM_BUNDLE_LOCATION;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;
import static org.osgi.framework.wiring.BundleRevision.HOST_NAMESPACE;
import static org.osgi.framework.wiring.BundleRevision.TYPE_FRAGMENT;

/**
 * <code>BundleUtils</code> provides utility methods for interacting with {@link Bundle Bundles}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 *
 */
public final class BundleUtils {

    /**
     * Queries whether the supplied {@link Bundle} is a fragment
     *
     * @param bundle the <code>Bundle</code>.
     * @return <code>true</code> if the <code>Bundle</code> is fragment, otherwise <code>false</code>.
     */
    public static boolean isFragmentBundle(Bundle bundle) {
        BundleRevision rev = bundle.adapt(BundleRevision.class);
        return rev != null && (rev.getTypes() & TYPE_FRAGMENT)!= 0;
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

    /**
     * Get exported packages of the supplied {@link Bundle}.
     *
     * @param bundle the <code>Bundle</code>.
     * @return <code>Set</code> of exported packages as plain strings.
     */
    public static Set<String> getExportedPackages(Bundle bundle) {
        Set<String> packagesExportedBySystemBundle = new HashSet<>(30);
        BundleWiring systemBundleWiring = bundle.adapt(BundleWiring.class);
        List<BundleCapability> packageWires = systemBundleWiring.getCapabilities(PACKAGE_NAMESPACE);
        for (BundleCapability packageWire : packageWires) {
            packagesExportedBySystemBundle.add((String) packageWire.getAttributes()
                    .get(PACKAGE_NAMESPACE));
        }
        return packagesExportedBySystemBundle;
    }

    /**
     * Get the packages exported by the given system {@link Bundle}.
     *
     * @param bundle the <code>Bundle</code>.
     * @return <code>Set<String></code> of exported packages if <code>bundle</code> is the system bundle.
     * @throws IllegalArgumentException if given bundle is not the system bundle or <code>null</code>.
     */
    public static Set<String> getPackagesExportedBySystemBundle(Bundle bundle) {
        if (!isSystemBundle(bundle)) {
            throw new IllegalArgumentException("Given bundle is not the system bundle");
        }
        return getExportedPackages(bundle);
    }

    /**
     * Get the host of the given fragment {@link Bundle}.
     *
     * @param fragment the <code>Bundle</code>.
     * @return <code>Set</code> containing the host if the <code>bundle</code> was a fragment.
     * @throws IllegalArgumentException if given bundle is not a fragment or <code>null</code>.
     */
    public static Set<Bundle> getHosts(Bundle fragment) {
        if (!isFragmentBundle(fragment)) {
            throw new IllegalArgumentException("Provided bundle is no fragment: " + fragment);
        }
        List<BundleWire> providedWires = fragment.adapt(BundleWiring.class).getProvidedWires(HOST_NAMESPACE);
        return providedWires.stream().map(bundleWire -> bundleWire.getProviderWiring().getBundle())
                .collect(toSet());
    }

    /**
     * Get set of bundles with the given symbolic name.
     *
     * @param bundleContext the <code>BundleContext</code>.
     * @param symbolicName the symbolic name.
     * @return <code>Set</code> containing the bundles with the given symbolic name.
     */
    public static Set<Bundle> getBundlesBySymbolicName(BundleContext bundleContext, String symbolicName) {
        return Arrays.stream(bundleContext.getBundles())
                .filter(bundle -> symbolicName.equals(bundle.adapt(BundleRevision.class).getSymbolicName()))
                .collect(toSet());
    }

    /**
     * Refresh the given set of bundles.
     *
     * @param bundleContext the <code>BundleContext</code>.
     * @param bundles to refresh.
     */
    public static void refreshBundles(BundleContext bundleContext, Set<Bundle> bundles) {
        // Equinox regions could hide bundle ID 0
        bundleContext.getBundle(SYSTEM_BUNDLE_LOCATION).adapt(FrameworkWiring.class).refreshBundles(bundles);
    }
}
