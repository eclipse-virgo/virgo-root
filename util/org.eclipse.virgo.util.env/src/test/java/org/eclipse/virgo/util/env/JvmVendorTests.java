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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.util.env.JvmVendor;
import org.junit.Test;

/**
 */
public class JvmVendorTests {

    @Test public void current() {
        String vendor = System.getProperty("java.vm.vendor");
        if (vendor.contains("Apple")) {
            assertEquals(JvmVendor.APPLE, JvmVendor.current());
        } else if (vendor.contains("Sun")) {
            assertEquals(JvmVendor.SUN, JvmVendor.current());
        }
    }

    @Test public void isOneOf() {
        assertTrue(JvmVendor.APPLE.isOneOf(JvmVendor.SUN, JvmVendor.APPLE));
        assertFalse(JvmVendor.APPLE.isOneOf(JvmVendor.SUN, JvmVendor.IBM));
    }
}
