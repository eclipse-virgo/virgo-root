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
        ServiceEvent serviceEvent = getServiceEvent(0);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(0, 1, 2);

    }

    @Test
    public void testEventFromKernel() {
        ServiceEvent serviceEvent = getServiceEvent(1);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(0, 1);

    }

    @Test
    public void testEventFromKernelWithImport() {
        this.regionServiceEventHook = new RegionServiceEventHook(getRegionMembership(), "java.lang.String", "");
        ServiceEvent serviceEvent = getServiceEvent(1);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(0, 1, 2);

    }

    @Test
    public void testEventFromUserRegion() {
        ServiceEvent serviceEvent = getServiceEvent(2);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(0, 2);

    }

    @Test
    public void testEventFromUserRegionWithExport() {
        this.regionServiceEventHook = new RegionServiceEventHook(getRegionMembership(), "", "java.lang.String");
        ServiceEvent serviceEvent = getServiceEvent(2);
        this.regionServiceEventHook.event(serviceEvent, this.bundleContexts);
        assertContextPresent(0, 1, 2);

    }

    private ServiceEvent getServiceEvent(int index) {
        return new ServiceEvent(ServiceEvent.REGISTERED, getServiceReference(index));
    }

    private void assertContextPresent(int... indices) {
        assertContextPresent(this.bundleContexts, indices);
    }
}
