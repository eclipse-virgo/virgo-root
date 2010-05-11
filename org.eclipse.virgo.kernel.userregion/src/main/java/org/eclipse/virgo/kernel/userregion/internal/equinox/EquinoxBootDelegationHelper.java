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

import java.util.ArrayList;

/**
 * A helper class for determinining whether or not Equinox will load a class via boot delegation.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
class EquinoxBootDelegationHelper {

    private final String[] exactPackageNames;

    private final String[] startsWithPackageNames;

    private final boolean allPackagesAreBootDelegated;

    /**
     * Create a new BootDelegationHelper that will provide boot delegation information for the given Equinox OSGi
     * Framework.
     * 
     * @param bootDelegationProperty The Equinox boot delegation property from which boot delegation information is to
     *        be derived.
     */
    public EquinoxBootDelegationHelper(String bootDelegationProperty) {

        boolean delegateAllPackages = false;
        ArrayList<String> stemMatches = new ArrayList<String>();
        ArrayList<String> exactMatches = new ArrayList<String>();

        if (bootDelegationProperty != null && bootDelegationProperty.trim().length() > 0) {

            String[] components = bootDelegationProperty.split(",");
            for (String component : components) {
                component = component.trim();
                if (component.equals("*")) {
                    delegateAllPackages = true;
                } else if (component.length() > 2 && component.endsWith(".*")) {
                    stemMatches.add(component.substring(0, component.length() - 2));
                } else {
                    exactMatches.add(component);
                }
            }
        }

        this.exactPackageNames = exactMatches.toArray(new String[exactMatches.size()]);
        this.startsWithPackageNames = stemMatches.toArray(new String[stemMatches.size()]);
        this.allPackagesAreBootDelegated = delegateAllPackages;
    }

    /**
     * Returns true if the class with the given name will be loaded via boot delegation
     * 
     * @param className The name of the class
     * @return Whether the class will be loaded via boot delegation.
     */
    public boolean isBootDelegated(String className) {
        if (this.allPackagesAreBootDelegated) {
            return true;
        } else {
            for (String packageStem : this.startsWithPackageNames) {
                if (className.startsWith(packageStem)) {
                    return true;
                }
            }
            String packageName = determinePackageName(className);
            if (packageName != null) {
                for (String exactPackage : this.exactPackageNames) {
                    if (packageName.equals(exactPackage)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String determinePackageName(String className) {
        int dotIndex = className.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == 0) {
            return null;
        } else {
            return className.substring(0, dotIndex);
        }
    }
}
