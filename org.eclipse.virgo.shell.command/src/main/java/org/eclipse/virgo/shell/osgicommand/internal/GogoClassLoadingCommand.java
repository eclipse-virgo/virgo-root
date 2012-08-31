/*******************************************************************************
 * Copyright (c) 2010-2011 SAP AG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Hristo Iliev, SAP AG - initial contribution
 *   Glyn Normington, VMware Inc. - bind commands to Gogo
 *******************************************************************************/

package org.eclipse.virgo.shell.osgicommand.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;
import org.eclipse.virgo.shell.osgicommand.helper.ClassLoadingHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Class loading commands for supportability and diagnostics
 */
public class GogoClassLoadingCommand {

    private BundleContext bundleContext;

    public GogoClassLoadingCommand(BundleContext context) {
        this.bundleContext = context;
    }

    /**
     * Lists all bundles that contain a class
     * 
     */
    @Descriptor("list all bundles that contain a class or resource with the specified name")
    public void clhas(@Descriptor("class or resource name") String className) {
        className = ClassLoadingHelper.convertToResourcePath(className);

        Map<Bundle, List<String>> foundBundles = ClassLoadingHelper.getBundlesContainingResource(bundleContext, className);
        if (foundBundles.size() == 0) {
            System.out.println("No bundle contains [" + className + "]");
            return;
        }

        outputBundlesAndResources("Bundles containing [" + className + "]:", foundBundles);
    }

    @Descriptor("list all bundles that can load the specified class")
    public void clload(@Descriptor("fully qualified class name") String className) {
        doClload(className, null);
    }

    @Descriptor("try to load the specified class using the specified bundle")
    public void clload(@Descriptor("fully qualified class name") String className, @Descriptor("bundle symbolic name") String bundleName) {
        doClload(className, bundleName);
    }

    @Descriptor("try to load the specified class using the specified bundle")
    public void clload(@Descriptor("fully qualified class name") String className, @Descriptor("  bundle id") long bundleId) {
        doClload(className, String.valueOf(bundleId));
    }

    private void doClload(String className, String bundle) {
        if (extractPackage(className) == null) {
            System.out.println("Warning: the class name [" + className + "] has no package and is assumed to belong to the default package");
        }

        Map<Bundle, Bundle> foundBundles;
        if (bundle == null) {
            foundBundles = ClassLoadingHelper.getBundlesLoadingClass(bundleContext, className);
        } else {
            foundBundles = ClassLoadingHelper.getBundlesLoadingClass(bundleContext, className, bundle);
        }

        if (foundBundles.size() == 0) {
            if (bundle == null) {
                System.out.println("No bundle can load class [" + className + "]");
            } else {
                System.out.println("Bundle [" + bundle + "] cannot load class [" + className + "]");
            }
            return;
        }

        outputFoundBundlesAndRelations("Successfully loaded [" + className + "] " + ((bundle != null) ? "using class loader from:" : "from:"),
            foundBundles, "provided by");
    }

    /**
     * Lists all bundles that export a class
     * 
     */
    @Descriptor("list all bundles that export a class with the specified name")
    public void clexport(@Descriptor("fully qualified class name") String className) {
        String classPackage = extractPackage(className);

        if (classPackage == null) {
            System.out.println("The class name [" + className + "] contains no package");
            return;
        }

        Bundle[] bundles = bundleContext.getBundles();
        HashMap<Long, String> foundBundles = new HashMap<Long, String>();
        for (Bundle bundle : bundles) {
            if (ClassLoadingHelper.isPackageExported(bundleContext, classPackage, bundle)) {
                if (ClassLoadingHelper.tryToLoadClass(className, bundle) != null) {
                    foundBundles.put(bundle.getBundleId(), bundle.getSymbolicName());
                } else {
                    foundBundles.put(bundle.getBundleId(), bundle.getSymbolicName() + "     [class not found, package only]");
                }
            }
        }

        if (foundBundles.size() == 0) {
            System.out.println("No bundle exports class [" + className + "]");
            return;
        }

        System.out.println();
        System.out.println("Bundles exporting [" + className + "]:");
        for (Map.Entry<Long, String> entry : foundBundles.entrySet()) {
            System.out.println("  " + entry.getKey() + "\t" + entry.getValue());
        }
    }

    private String extractPackage(String className) {
        int index = className.lastIndexOf(".");
        return index == -1 ? null : className.substring(0, index);        
    }

    /**
     * Outputs a list with all found bundles
     * 
     * @param message Message to print before the list
     * @param foundBundles A map with ID and bundle details
     * @param relation Relation between the bundles
     */
    private void outputFoundBundlesAndRelations(String message, Map<Bundle, Bundle> foundBundles, String relation) {
        System.out.println();
        System.out.println(message);

        for (Map.Entry<Bundle, Bundle> entry : foundBundles.entrySet()) {
            Bundle testBundle = entry.getKey();
            Bundle originalBundle = entry.getValue();
            if (testBundle.equals(originalBundle)) {
                System.out.println("  " + bundleToString(testBundle, false));
            } else {
                System.out.println("  " + bundleToString(testBundle, false));
                if (relation != null)
                    System.out.println("  \t\t[" + relation + " " + bundleToString(originalBundle, true) + "]");
            }
        }
    }

    /**
     * Outputs a list with all found bundles
     * 
     * @param message Message to print before the list
     * @param foundBundles A map with Bundle and found resources
     */
    private void outputBundlesAndResources(String message, Map<Bundle, List<String>> foundBundles) {
        System.out.println();
        System.out.println(message);

        for (Map.Entry<Bundle, List<String>> bundleListEntry : foundBundles.entrySet()) {
            System.out.println("  " + bundleToString(bundleListEntry.getKey(), false));

            for (String resource : bundleListEntry.getValue()) {
                System.out.println("  \t\t" + resource);
            }
        }
    }

    /**
     * Provides String representation of a Bundle
     * 
     * @param b See {@link org.osgi.framework.Bundle}
     * @param space Separate ID and symbolic name with space instead of tab character
     * @return String containing ID and symbolic name of the bundle
     */
    private String bundleToString(Bundle b, boolean space) {
        return b.getBundleId() + (space ? " " : "\t") + b.getSymbolicName();
    }

}
