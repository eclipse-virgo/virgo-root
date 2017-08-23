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

import org.eclipse.virgo.shell.osgicommand.helper.ClassLoadingHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.*;

/**
 * MBean for class loading queries
 */
public class ClassLoadingSupport implements ClassLoadingSupportMBean {

    private BundleContext bundleContext;

    public ClassLoadingSupport(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public Map<List<String>, List<String>> getBundlesContainingResource(String resourcePattern) {
        Map<Bundle, List<String>> resources = ClassLoadingHelper.getBundlesContainingResource(this.bundleContext, resourcePattern);

        Map<List<String>, List<String>> result = new HashMap<List<String>, List<String>>();

        for (Map.Entry<Bundle, List<String>> entry: resources.entrySet()) {
            result.put(getBundleInformation(entry.getKey()), entry.getValue());
        }

        return result;
    }

    @Override
    public Map<List<String>, List<String>> getBundlesLoadingClass(String className) {
        Map<Bundle, Bundle> bundles = ClassLoadingHelper.getBundlesLoadingClass(this.bundleContext, className);

        Map<List<String>, List<String>> result = new HashMap<List<String>, List<String>>();

        for (Map.Entry<Bundle, Bundle> entry : bundles.entrySet()) {
            Bundle loadingBundle = entry.getKey();
            Bundle originatingBundle = entry.getKey();
            result.put(getBundleInformation(loadingBundle), getBundleInformation(originatingBundle));
        }

        return result;
    }

    @Override
    public List<List<String>> getBundlesExportingPackage(String packageName) {
        Bundle[] allBundles = this.bundleContext.getBundles();

        List<List<String>> result = new ArrayList<List<String>>();

        for (Bundle bundle : allBundles) {
            if (ClassLoadingHelper.isPackageExported(this.bundleContext, packageName, bundle)) {
                result.add(getBundleInformation(bundle));
            }
        }

        return result;
    }

    @Override
    public boolean tryToLoadClassFromBundle(String className, long bundleId) {
        Class<?> result = ClassLoadingHelper.tryToLoadClass(className, this.bundleContext.getBundle(bundleId));
        return result != null;
    }

    /**
     * Builds bundle information (ID, symbolic name) to a collection
     *
     * @param bundle Bundle to obtain information for. If <code>null</code>, then the collection will be filled
     *               with null elements
     * @return List with bundle information as string or <code>null</code> if the bundle is <code>null</code>
     */
    private List<String> getBundleInformation(Bundle bundle) {
        List<String> list = new ArrayList<String>(2);

        list.add(bundle != null ? "" + bundle.getBundleId() : null);
        list.add(bundle != null ? bundle.getSymbolicName() : null);

        return list;
    }

}
