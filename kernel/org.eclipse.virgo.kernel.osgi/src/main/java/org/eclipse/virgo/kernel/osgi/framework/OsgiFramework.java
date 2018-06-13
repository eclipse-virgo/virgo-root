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

package org.eclipse.virgo.kernel.osgi.framework;

import java.io.File;
import java.util.Collection;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.FrameworkWiring;

/**
 * <code>OsgiFramework</code> defines a basic abstraction for interacting with various OSGi implementations in a
 * server-independent manner.
 * <p/>
 * 
 * Implementations <strong>must</strong> make themselves available in the OSGi service registry under the
 * <code>OsgiFramework</code> class names.
 * 
 */
public interface OsgiFramework {

    /**
     * Name of the {@link Manifest} entry defining the scope of a bundle.
     */
    public static String HEADER_MODULE_SCOPE = "Module-Scope";

    /**
     * Gets the {@link BundleContext} of the system {@link Bundle}.
     * 
     * @return the system <code>BundleContext</code>.
     */
    BundleContext getBundleContext();

    /**
     * Gets the {@link ClassLoader} for the supplied {@link Bundle}.
     * 
     * @param bundle the <code>Bundle</code>.
     * @return the <code>ClassLoader</code>.
     */
    ClassLoader getBundleClassLoader(Bundle bundle);

    /**
     * Returns true if the class with the given name will be loaded via boot delegation by the underlying OSGi framework
     * 
     * @param className The class name
     * @return Whether the underlying framework considers the class to be boot delegated.
     */
    boolean isBootDelegated(String className);

    /**
     * Returns all of the supplied bundle's direct dependencies. A bundle's direct dependencies are the bundles to which
     * its package imports have been wired. Equivalent to calling <code>getDirectDependencies(bundle, false)</code>.
     * 
     * @param bundle The bundle for which the direct dependencies are required
     * @return the supplied bundle's direct dependencies
     */
    Bundle[] getDirectDependencies(Bundle bundle);

    /**
     * Returns all of the supplied bundle's direct dependencies, optionally including the fragments, if any, of each
     * direct dependency in the returned array. A bundle's direct dependencies are the bundles to which its package
     * imports have been wired.
     * 
     * @param bundle The bundle for which the direct dependencies are required
     * @param includeFragments <code>true</code> if fragments of bundles to which package imports have been wired should
     *        be included in the returned array
     * @return the supplied bundle's direct dependencies
     */
    Bundle[] getDirectDependencies(Bundle bundle, boolean includeFragments);

    /**
     * Refreshes the supplied {@link Bundle}. Reloads all contents, and refreshes the packages.
     * 
     * @param bundle the <code>Bundle</code> to refresh.
     * @throws BundleException if there is an error during refresh.
     * @see FrameworkWiring#refreshBundles(Collection, FrameworkListener...)
     */
    void refresh(Bundle bundle) throws BundleException;

    /**
     * Updates the supplied <code>Bundle</code> from the supplied file or directory using the supplied
     * <code>ManifestTransformer</code> to transformer the <code>Bundle</code>'s manifest as it is updated.
     * 
     * @param bundle the bundle to update
     * @param manifestTransformer the manifest transformer to apply to the bundle's manifest
     * @param location the file or directory containing the updated bundle contents
     * 
     * @throws BundleException if the bundle fails to update
     */
    void update(Bundle bundle, ManifestTransformer manifestTransformer, File location) throws BundleException;
}
