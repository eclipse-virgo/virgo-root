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

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;

public class RegionBundleEventHookTests extends AbstractRegionHookTest {

    private RegionBundleEventHook regionBundleEventHook;

    private Collection<BundleContext> bundleContexts;

    @Before
    public void setUp() throws Exception {
        this.regionBundleEventHook = new RegionBundleEventHook(getRegionMembership());
        this.bundleContexts = getBundleContexts();
    }

    @Test
    public void testEventFromSystemBundle() {
        BundleEvent event = getBundleEvent(SYSTEM_BUNDLE_INDEX);
        this.regionBundleEventHook.event(event, this.bundleContexts);
        assertContextPresent(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
    }

    @Test
    public void testEventFromKernelRegion() {
        BundleEvent event = getBundleEvent(KERNEL_BUNDLE_INDEX);
        this.regionBundleEventHook.event(event, this.bundleContexts);
        assertContextPresent(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX);
    }

    @Test
    public void testEventFromUserRegion() {
        BundleEvent event = getBundleEvent(USER_REGION_BUNDLE_INDEX);
        this.regionBundleEventHook.event(event, this.bundleContexts);
        assertContextPresent(SYSTEM_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
    }

    private BundleEvent getBundleEvent(int bundleIndex) {
        return new BundleEvent(BundleEvent.INSTALLED, getBundle(bundleIndex));
    }
    
    private void assertContextPresent(int... indices) {
        assertContextPresent(this.bundleContexts, indices);
    }

}
