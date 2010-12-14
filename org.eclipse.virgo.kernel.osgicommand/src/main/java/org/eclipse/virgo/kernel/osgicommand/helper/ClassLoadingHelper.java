/*******************************************************************************
 * Copyright (c) 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Hristo Iliev, SAP AG - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.kernel.osgicommand.helper;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.util.HashMap;

/**
 * Helper for class loading supporting commands
 */
@SuppressWarnings("deprecation")
public class ClassLoadingHelper {

    /**
     * Determines if a class package is exported by a bundle
     *
     * @param bundleContext Bundle context for interaction with the OSGi framework
     * @param classPackage  Class package to check for
     * @param testBundle    The bundle that has to be tested
     * @return TRUE if the bundle is exported by the package, FALSE if the class is not exported or it does not have a package
     */
    public static boolean isPackageExported(BundleContext bundleContext, String classPackage, Bundle testBundle) {
        ServiceReference<PlatformAdmin> reference = bundleContext.getServiceReference(PlatformAdmin.class);
        PlatformAdmin platformAdmin = bundleContext.getService(reference);
        BundleDescription bundleDescription = platformAdmin.getState(false).getBundle(testBundle.getBundleId());

        ExportPackageDescription[] exportDescriptions = bundleDescription.getSelectedExports();
        for (ExportPackageDescription exportDescription : exportDescriptions) {
            if (exportDescription.getName().equals(classPackage))
                return true;
        }

        // not found
        return false;
    }

    /**
     * Returns the bundles that can load a class and the originating bundle in a map
     *
     * @param bundleContext Bundle context for interaction with the OSGi framework
     * @param className     Fully qualified class name (in the form &lt;package&gt;.&lt;class name&gt;)
     * @return Map between the bundles that can load the class and the bundle that provides it in each case
     */
    public static HashMap<Bundle, Bundle> getBundlesLoadingClass(BundleContext bundleContext, String className) {
        Bundle[] bundles = bundleContext.getBundles();
        HashMap<Bundle, Bundle> foundBundles = new HashMap<Bundle, Bundle>();
        for (Bundle bundle : bundles) {
            Bundle originBundle = originBundleOfClass(className, bundleContext, bundle);
            if (originBundle != null) {
                foundBundles.put(bundle, originBundle);
            }
        }

        return foundBundles;
    }

    /**
     * Find the originating bundle of a class loaded by a bundle
     * 
     * @param className Fully qualified class name (in the form &lt;package&gt;.&lt;class name&gt; name)
     * @param bundleContext Bundle context for interaction with the OSGi framework
     * @param loadingBundle Bundle instance to load class from
     * @return originating {@link Bundle} or null if it cannot be loaded by <code>testBundle</code>
     */
    private static Bundle originBundleOfClass(String className, BundleContext bundleContext, Bundle loadingBundle) {
        Class<?> clasz = tryToLoadClass(className, loadingBundle);
        Bundle originBundle = null;
        if (clasz != null) {
            originBundle = FrameworkUtil.getBundle(clasz);
            if (originBundle == null) {
                // this is the system bundle
                originBundle = bundleContext.getBundle(0);
            }
        }
        return originBundle;
    }

    /**
     * Tries to load a class
     *
     * @param className Fully qualified class name (in the form &lt;package&gt;.&lt;class name&gt;)
     * @param bundle    Bundle instance that has to be checked
     * @return The loaded class or null if it cannot be loaded from this bundle
     */
    public static Class<?> tryToLoadClass(String className, Bundle bundle) {
        if (bundle == null)
            return null;

        try {
            return bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            // do nothing - if the class is not found we don't care
        }
        return null;
    }

    /**
     * Returns all bundles that can load a class
     *
     * @param bundleContext Bundle context for interaction with the OSGi framework
     * @param className     Fully qualified class name (in the form &lt;package&gt;.&lt;class name&gt;)
     * @param bundle        Bundle name or ID that has to be checked
     * @return Map between the bundle that can load the class (key) and the one that provides it (value)
     * @throws IllegalArgumentException if there is no bundle with such name/id
     */
    public static HashMap<Bundle, Bundle> getBundlesLoadingClass(BundleContext bundleContext, String className, String bundle) throws IllegalArgumentException {
        HashMap<Bundle, Bundle> result = new HashMap<Bundle, Bundle>();
        long id = Long.MIN_VALUE;
        try {
            id = Long.parseLong(bundle);
        } catch (NumberFormatException e) {
            // not a number - then it is bundle name
        }

        if (id >= 0) {
            Bundle testBundle = bundleContext.getBundle(id);
            if (testBundle == null)
                throw new IllegalArgumentException("Bundle with ID [" + id + "] not found");
            
            Bundle originBundle = originBundleOfClass(className, bundleContext, testBundle);
            if (originBundle !=null) {
                result.put(testBundle, originBundle);
            }
        } else {
            ServiceReference<PackageAdmin> reference = bundleContext.getServiceReference(PackageAdmin.class);
            PackageAdmin packageAdmin = bundleContext.getService(reference);
            Bundle[] bundles = packageAdmin.getBundles(bundle, null);
            if (bundles == null)
                throw new IllegalArgumentException("Bundle with symbolic name [" + bundle + "] not found");

            for (Bundle testBundle : bundles) {
                Bundle originBundle = originBundleOfClass(className, bundleContext, testBundle);
                if (originBundle !=null) {
                    result.put(testBundle, originBundle);
                }
            }
        }

        return result;
    }

}