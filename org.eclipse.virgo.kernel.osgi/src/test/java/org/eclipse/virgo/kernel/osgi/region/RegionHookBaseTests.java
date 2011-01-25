/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class RegionHookBaseTests extends AbstractRegionHookTest {

    TestRegionHook testRegionHook;
    
    @Before
    public void setUp() {
        this.testRegionHook = new TestRegionHook(getRegionMembership());
    }

    @Test
    public void testIsSystemBundleBundleContext() {
        Assert.assertTrue(RegionHookBase.isSystemBundle(getBundleContext(SYSTEM_BUNDLE_INDEX)));
        Assert.assertFalse(RegionHookBase.isSystemBundle(getBundleContext(KERNEL_BUNDLE_INDEX)));
    }

    @Test
    public void testIsSystemBundleBundle() {
        Assert.assertTrue(RegionHookBase.isSystemBundle(getBundle(SYSTEM_BUNDLE_INDEX)));
        Assert.assertFalse(RegionHookBase.isSystemBundle(getBundle(KERNEL_BUNDLE_INDEX)));
    }

    @Test
    public void testgetRegionBundleContext() {
        Assert.assertNull(this.testRegionHook.getRegion(getBundleContext(SYSTEM_BUNDLE_INDEX)));
        Assert.assertEquals(getKernelRegion(), this.testRegionHook.getRegion(getBundleContext(KERNEL_BUNDLE_INDEX)));
        Assert.assertFalse(getKernelRegion().equals(this.testRegionHook.getRegion(getBundleContext(USER_REGION_BUNDLE_INDEX))));
    }

    @Test
    public void testgetRegionBundle() {
        Assert.assertNull(this.testRegionHook.getRegion(getBundle(SYSTEM_BUNDLE_INDEX)));
        Assert.assertEquals(getKernelRegion(), this.testRegionHook.getRegion(getBundle(KERNEL_BUNDLE_INDEX)));
        Assert.assertFalse(getKernelRegion().equals(this.testRegionHook.getRegion(getBundle(USER_REGION_BUNDLE_INDEX))));
    }

    
    @Test
    public void testgetRegionLong() {
        Assert.assertNull(this.testRegionHook.getRegion(getBundleId(SYSTEM_BUNDLE_INDEX)));
        Assert.assertEquals(getKernelRegion(), this.testRegionHook.getRegion(getBundleId(KERNEL_BUNDLE_INDEX)));
        Assert.assertFalse(getKernelRegion().equals(this.testRegionHook.getRegion(getBundleId(USER_REGION_BUNDLE_INDEX))));
    }

    class TestRegionHook extends RegionHookBase {

        private TestRegionHook(RegionMembership regionMembership) {
            super(regionMembership);
        }

    }

}
