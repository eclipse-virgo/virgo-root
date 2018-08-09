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
package org.eclipse.virgo.shell.osgicommand.helper;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.net.URL;
import java.util.*;

/**
 * Helper for class loading supporting commands
 */
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
            if (exportDescription.getName().equals(convertToClassName(classPackage)))
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
    public static Map<Bundle, Bundle> getBundlesLoadingClass(BundleContext bundleContext, String className) {
        Bundle[] bundles = bundleContext.getBundles();
        HashMap<Bundle, Bundle> foundBundles = new HashMap<>();
        for (Bundle bundle : bundles) {
            Bundle originBundle = getOriginBundleOfClass(className, bundleContext, bundle);
            if (originBundle != null) {
                foundBundles.put(bundle, originBundle);
            }
        }

        return foundBundles;
    }

    /**
     * Find the originating bundle of a class loaded by a bundle
     *
     * @param className     Fully qualified class name (in the form &lt;package&gt;.&lt;class name&gt; name)
     * @param bundleContext Bundle context for interaction with the OSGi framework
     * @param loadingBundle Bundle instance to load class from
     * @return originating {@link Bundle} or null if it cannot be loaded by <code>testBundle</code>
     */
    private static Bundle getOriginBundleOfClass(String className, BundleContext bundleContext, Bundle loadingBundle) {
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
            return bundle.loadClass(convertToClassName(className));
        } catch (ClassNotFoundException e) {
            // do nothing - if the class is not found we don't care
        }
        return null;
    }

    /**
     * Converts resource path (/javax/servlet/Servlet.class) to class name (javax.servlet.Servlet)
     *
     * @param resourcePath Path to the resource
     * @return Class name
     */
    static String convertToClassName(String resourcePath) {
        if (resourcePath == null)
            return null;

        resourcePath = resourcePath.replace("/", ".");
        if (resourcePath.startsWith(".")) {
            resourcePath = resourcePath.substring(1);
        }
        if (resourcePath.endsWith(".class")) {
            resourcePath = resourcePath.substring(0, resourcePath.length() - 6);
        }

        return resourcePath;
    }

    /**
     * Convert from package to path format (javax.servlet.Servlet --> javax/servlet/Servlet.class)
     *
     * @param className Class name
     * @return Path to a resource
     */
    public static String convertToResourcePath(String className) {
       if (className == null)
           return null;

        String result = className;
        if (!className.contains("/") && !className.contains("*")) {
            if (className.endsWith(".class")) {
                result = className.substring(0, className.length() - 6);
            }
            return result.replace(".", "/") + ".class";
        }

        return result;
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
    public static Map<Bundle, Bundle> getBundlesLoadingClass(BundleContext bundleContext, String className, String bundle) throws IllegalArgumentException {
        HashMap<Bundle, Bundle> result = new HashMap<>();
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

            Bundle originBundle = getOriginBundleOfClass(className, bundleContext, testBundle);
            if (originBundle != null) {
                result.put(testBundle, originBundle);
            }
        } else {
            ServiceReference<PackageAdmin> reference = bundleContext.getServiceReference(PackageAdmin.class);
            PackageAdmin packageAdmin = bundleContext.getService(reference);
            Bundle[] bundles = packageAdmin.getBundles(bundle, null);
            if (bundles == null)
                throw new IllegalArgumentException("Bundle with symbolic name [" + bundle + "] not found");

            for (Bundle testBundle : bundles) {
                Bundle originBundle = getOriginBundleOfClass(className, bundleContext, testBundle);
                if (originBundle != null) {
                    result.put(testBundle, originBundle);
                }
            }
        }

        return result;
    }

    /**
     * Returns all bundles that contain a class
     *
     * @param bundleContext   Bundle context for interaction with the OSGi framework
     * @param resourcePattern Search pattern in the form &lt;package&gt;/&lt;class name&gt;. The pattern can contain wildcards
     * @return Map between the bundle (key) and the URL(s) of the resources (value)
     */
    public static Map<Bundle, List<String>> getBundlesContainingResource(BundleContext bundleContext, String resourcePattern) {
        Map<Bundle, List<String>> result = new HashMap<>();

        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            List<String> entries = findEntries(bundle, resourcePattern);
            if (entries.size() != 0) {
                result.put(bundle, entries);
            }
        }

        return result;
    }

    /**
     * Returns a list with bundle entries matching a resource pattern
     *
     * @param bundle Bundle to scan for entries
     * @param resourcePattern Pattern used for matching
     * @return List with found entries
     */
    private static List<String> findEntries(Bundle bundle, String resourcePattern) {
        HashSet<String> urls = new HashSet<>();

        int index = resourcePattern.lastIndexOf("/");
        if (index != -1) {
            String resourcePath = resourcePattern.substring(0, index);
            String resourceEntity = resourcePattern.substring(index + 1);
            // Search the whole bundle for entity starting from the root. We need this since "the pattern is only
            // matched against the last element of the entry path" as stated in findEntries JavaDoc. This means that
            // web bundle that packages a class in WEB-INF/classes will not be found by findEntries since the path is
            // prepended with WEB-INF/classes. Therefore we search for a class everywhere in the bundle and then
            // filter the result.
            addURLs(urls, bundle.findEntries("/", resourceEntity, true), resourcePath);
        }

        // Search the root of the bundle for entity matching the specified pattern
        addURLs(urls, bundle.findEntries("/", resourcePattern, true), null);
        return new ArrayList<>(urls);
    }

    /**
     * Adds all found resources eliminating the duplicates or the ones that do not contain the requested path
     *
     * @param urls      Result set with URLs as string
     * @param foundURLs Enumeration to scan
     * @param path      Expected path of the entities. The entities are not put in the result set unless they contain
     *                  this path.
     */
    private static void addURLs(HashSet<String> urls, Enumeration<URL> foundURLs, String path) {
        if (foundURLs != null) {
            while (foundURLs.hasMoreElements()) {
                String url = foundURLs.nextElement().getFile();
                if (path != null && !url.contains(path)) {
                    continue;
                }
                urls.add(url);
            }
        }
    }

}