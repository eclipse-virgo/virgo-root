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
        BundleEvent event = getBundleEvent(0);
        this.regionBundleEventHook.event(event, this.bundleContexts);
        assertContextPresent(0, 1, 2);
    }

    @Test
    public void testEventFromKernelRegion() {
        BundleEvent event = getBundleEvent(1);
        this.regionBundleEventHook.event(event, this.bundleContexts);
        assertContextPresent(0, 1);
    }

    @Test
    public void testEventFromUserRegion() {
        BundleEvent event = getBundleEvent(2);
        this.regionBundleEventHook.event(event, this.bundleContexts);
        assertContextPresent(0, 2);
    }

    private BundleEvent getBundleEvent(int bundleIndex) {
        return new BundleEvent(BundleEvent.INSTALLED, getBundle(bundleIndex));
    }
    
    private void assertContextPresent(int... indices) {
        assertContextPresent(this.bundleContexts, indices);
    }

}
