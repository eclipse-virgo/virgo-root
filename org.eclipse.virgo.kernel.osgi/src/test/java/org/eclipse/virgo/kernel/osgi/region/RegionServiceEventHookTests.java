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
import org.osgi.framework.ServiceEvent;

public class RegionServiceEventHookTests extends AbstractRegionServiceHookTest {

    private RegionServiceEventHook regionServiceEventHook;

    private Collection<BundleContext> bundleContexts;

    @Before
    public void setUp() throws Exception {
        this.regionServiceEventHook = new RegionServiceEventHook(getRegionMembership(), "", "");
        this.bundleContexts = getBundleContexts();
    }

    @Test
    public void testEventFromSystemBundle() {
        ServiceEvent serviceEvent = getServiceEvent(SYSTEM_BUNDLE_INDEX);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);

    }

    @Test
    public void testEventFromKernel() {
        ServiceEvent serviceEvent = getServiceEvent(KERNEL_BUNDLE_INDEX);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX);

    }

    @Test
    public void testEventFromKernelWithImport() {
        this.regionServiceEventHook = new RegionServiceEventHook(getRegionMembership(), "java.lang.String", "");
        ServiceEvent serviceEvent = getServiceEvent(KERNEL_BUNDLE_INDEX);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);

    }

    @Test
    public void testEventFromUserRegion() {
        ServiceEvent serviceEvent = getServiceEvent(USER_REGION_BUNDLE_INDEX);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(SYSTEM_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);

    }

    @Test
    public void testEventFromUserRegionWithExport() {
        this.regionServiceEventHook = new RegionServiceEventHook(getRegionMembership(), "", "java.lang.String");
        ServiceEvent serviceEvent = getServiceEvent(USER_REGION_BUNDLE_INDEX);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);

    }

    private ServiceEvent getServiceEvent(int index) {
        return new ServiceEvent(ServiceEvent.REGISTERED, getServiceReference(index));
    }

    private void assertContextPresent(int... indices) {
        assertContextPresent(this.bundleContexts, indices);
    }
}
