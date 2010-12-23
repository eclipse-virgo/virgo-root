/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
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
        Assert.assertTrue(RegionHookBase.isSystemBundle(getBundle(0).getBundleContext()));
        Assert.assertFalse(RegionHookBase.isSystemBundle(getBundle(1).getBundleContext()));
    }

    @Test
    public void testIsSystemBundleBundle() {
        Assert.assertTrue(RegionHookBase.isSystemBundle(getBundle(0)));
        Assert.assertFalse(RegionHookBase.isSystemBundle(getBundle(1)));
    }

    @Test
    public void testIsUserRegionBundleBundleContext() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundle(0).getBundleContext()));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(getBundle(1).getBundleContext()));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundle(2).getBundleContext()));
    }

    @Test
    public void testIsUserRegionBundleBundle() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundle(0)));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(getBundle(1)));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundle(2)));
    }

    @Test
    public void testIsUserRegionBundleLong() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundle(0).getBundleId()));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(getBundle(1).getBundleId()));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(getBundle(2).getBundleId()));
    }

    class TestRegionHook extends RegionHookBase {

        private TestRegionHook(RegionMembership regionMembership) {
            super(regionMembership);
        }

    }

}
