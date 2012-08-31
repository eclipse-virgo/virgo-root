/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hristo Iliev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.shell.osgicommand.management;

import java.util.List;
import java.util.Map;

/**
 * mBean interface for class loading queries
 */
public interface ClassLoadingSupportMBean {

    /**
     * Returns list with all bundles that can load a class and the origin bundle
     *
     * @param resourcePattern Resource pattern (package.class) to search for. The pattern may contain wildcard (*)
     * @return Mapping between the bundle that contains the resource (possibly provided by fragment bundle) and the
     *         resource URLs found in the bundle. The bundle information list contain the bundle ID and
     *         symbolic name.
     */
    Map<List<String>, List<String>> getBundlesContainingResource(String resourcePattern);

    /**
     * Returns list with all bundles that can load a class and the origin bundle
     *
     * @param className Fully qualified class name (package.class) to load
     * @return Mapping between the bundle that can load the class (possibly delegating to another bundle) and the
     *         origin bundle that actually performs the loading. The bundle information list contain the bundle ID and
     *         symbolic name.
     */
    Map<List<String>, List<String>> getBundlesLoadingClass(String className);

    /**
     * Returns list with all bundles that export a package
     *
     * @param packageName Package to scan for
     * @return List with information (ID and symbolic name) about the bundles that export the package
     */
    List<List<String>> getBundlesExportingPackage(String packageName);

    /**
     * Tries to load a class from bundle
     *
     * @param className Fully qualified class name (package.class) to load
     * @param bundleId  Bundle ID of the bundle, which loader will be used to load the class
     * @return <code>true</code> if the class can be loaded, <code>false</code> if the class cannot be loaded with the
     *         bundle's loader
     */
    public boolean tryToLoadClassFromBundle(String className, long bundleId);
}
