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

package org.eclipse.virgo.util.env;

/**
 * Identifies the vendor of a JVM implementation. The vendor of the currently running JVM can be accessed from
 * {@link #current()}. When no vendor can be determined, the vendor is identified as {@link #UNKNOWN}. <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public enum JvmVendor {
    /**
     * Sun Microsystems.
     */
    SUN("Sun"),

    /**
     * Apple.
     */
    APPLE("Apple"),

    /**
     * IBM.
     */
    IBM("IBM"),

    /**
     * BEA Systems.
     */
    BEA("BEA"),

    /**
     * Unknown.
     */
    UNKNOWN("unknown");

    private static final JvmVendor current;

    private final String identifer;

    static {
        String vmVendor = System.getProperty("java.vm.vendor");
        JvmVendor foundVendor = null;
        for (JvmVendor vendor : values()) {
            if (vmVendor.contains(vendor.identifer)) {
                foundVendor = vendor;
                break;
            }
        }
        current = foundVendor == null ? UNKNOWN : foundVendor;
    }

    private JvmVendor(String identifer) {
        this.identifer = identifer;
    }

    /**
     * Gets the identifier for the vendor of the currently running JVM.
     * 
     * @return the identifier for the JVM vendor.
     */
    public static JvmVendor current() {
        return current;
    }

    /**
     * Indicates whether this <code>JvmVendor</code> is one of the supplied list of vendors.
     * 
     * @param jvmVendors the vendors to check against.
     * @return <code>true</code> if this <code>JvmVendor</code> is in the supplied list, otherwise
     *         <code>false</code>.
     */
    public boolean isOneOf(JvmVendor... jvmVendors) {
        for (JvmVendor jvmVendor : jvmVendors) {
            if (this.equals(jvmVendor)) {
                return true;
            }
        }
        return false;
    }
}
