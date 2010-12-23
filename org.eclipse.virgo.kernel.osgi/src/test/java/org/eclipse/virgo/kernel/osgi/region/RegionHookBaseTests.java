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

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

public class RegionHookBaseTests {

    private StubBundle b0;

    private StubBundle b1;

    private StubBundle b2;

    private TestRegionHook testRegionHook;

    @Before
    public void setUp() {
        // Bundle id 1 is not in the region membership and bundle ids 0, 2, 3, ... are in the region.
        this.testRegionHook = new TestRegionHook(new StubRegionMembership(2L));
        this.b0 = new StubBundle(0L, "system", new Version("0"), "system@0");
        this.b1 = new StubBundle(1L, "one", new Version("1"), "kernel@1");
        this.b2 = new StubBundle(2L, "two", new Version("2"), "kernel@2");
    }

    @Test
    public void testIsSystemBundleBundleContext() {
        Assert.assertTrue(RegionHookBase.isSystemBundle(this.b0.getBundleContext()));
        Assert.assertFalse(RegionHookBase.isSystemBundle(this.b1.getBundleContext()));
    }

    @Test
    public void testIsSystemBundleBundle() {
        Assert.assertTrue(RegionHookBase.isSystemBundle(this.b0));
        Assert.assertFalse(RegionHookBase.isSystemBundle(this.b1));
    }

    @Test
    public void testIsUserRegionBundleBundleContext() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(this.b0.getBundleContext()));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(this.b1.getBundleContext()));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(this.b2.getBundleContext()));
    }

    @Test
    public void testIsUserRegionBundleBundle() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(this.b0));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(this.b1));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(this.b2));
    }

    @Test
    public void testIsUserRegionBundleLong() {
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(this.b0.getBundleId()));
        Assert.assertFalse(this.testRegionHook.isUserRegionBundle(this.b1.getBundleId()));
        Assert.assertTrue(this.testRegionHook.isUserRegionBundle(this.b2.getBundleId()));
    }

    private class TestRegionHook extends RegionHookBase {

        private TestRegionHook(RegionMembership regionMembership) {
            super(regionMembership);
        }

    }

}
