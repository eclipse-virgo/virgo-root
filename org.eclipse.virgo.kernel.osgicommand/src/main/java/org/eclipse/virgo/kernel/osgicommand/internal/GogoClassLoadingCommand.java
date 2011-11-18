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

package org.eclipse.virgo.kernel.osgicommand.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.kernel.osgicommand.helper.ClassLoadingHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Class loading commands for supportability and diagnostics
 */
public class GogoClassLoadingCommand {

    public static final String NEW_LINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

    private BundleContext bundleContext;

    public GogoClassLoadingCommand(BundleContext context) {
        this.bundleContext = context;
    }

    /**
     * Lists all bundles that contain a class
     * 
     */
    public void clhas(String className) {
        
        className = ClassLoadingHelper.convertToResourcePath(className);

        Map<Bundle, List<String>> foundBundles = ClassLoadingHelper.getBundlesContainingResource(bundleContext, className);
        if (foundBundles.size() == 0) {
            System.out.println("No bundle contains [" + className + "]");
            return;
        }

        outputBundlesAndResources("Bundles containing [" + className + "]:", foundBundles);
    }
    
    public void clload(String className) {
        doClload(className, null);
    }
    
    public void clload(String className, String bundleName) {
        doClload(className, bundleName);
    }
    
    public void clload(String className, long bundleId) {
        doClload(className, String.valueOf(bundleId));
    }

    private void doClload(String className, String bundle) {
        
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

        outputFoundBundlesAndRelations("Successfully loaded [" + className + "] "
            + ((bundle != null) ? "using class loader from:" : "from:"), foundBundles, "provided by");
    }

    /**
     * Lists all bundles that export a class
     * 
     */
    public void clexport(String className) {
        // Check if the class has a package
        int index = className.lastIndexOf(".");
        if (index == -1) {
            System.out.println("The class name [" + className + "] contains no package");
            return;
        }
        String classPackage = className.substring(0, index);

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

    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("---");
        help.append("Classloading Commands");
        help.append("---");
        help.append(NEW_LINE);
        help.append("\tclhas <class name> - lists all bundles that contain a class with the specified name.").append(NEW_LINE);
        help.append(
            "\tclload <class name> [<bundle id> | <bundle name>]- lists all bundles that can load a class or tries to load the class with specified bundle.").append(
            NEW_LINE);
        help.append("\tclexport <class name> - lists all bundles that export a class with the specified name.").append(NEW_LINE);
        return help.toString();
    }

}
