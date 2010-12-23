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
import org.osgi.framework.ServiceReference;

public class RegionServiceFindHookTests extends AbstractRegionServiceHookTest {

    private RegionServiceFindHook regionServiceFindHook;

    private Collection<ServiceReference<?>> serviceReferences;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.regionServiceFindHook = new RegionServiceFindHook(getRegionMembership(), "", "");
        this.serviceReferences = getServiceReferences();
    }

    @Test
    public void testFindBySystemBundle() {
        this.regionServiceFindHook.find(getBundleContext(0), null, null, true, this.serviceReferences);
        assertServiceReferencePresent(0, 1, 2);
    }

    @Test
    public void testFindByKernelBundle() {
        this.regionServiceFindHook.find(getBundleContext(1), null, null, true, this.serviceReferences);
        assertServiceReferencePresent(0, 1);
    }

    @Test
    public void testFindByKernelBundleWithExport() {
        this.regionServiceFindHook = new RegionServiceFindHook(getRegionMembership(), "", "java.lang.String");
        this.regionServiceFindHook.find(getBundleContext(1), null, null, true, this.serviceReferences);
        assertServiceReferencePresent(0, 1, 2);
    }

    @Test
    public void testFindByUserRegionBundle() {
        this.regionServiceFindHook.find(getBundleContext(2), null, null, true, this.serviceReferences);
        assertServiceReferencePresent(0, 2);
    }

    @Test
    public void testFindByUserRegionBundleWithImport() {
        this.regionServiceFindHook = new RegionServiceFindHook(getRegionMembership(), "java.lang.String", "");
        this.regionServiceFindHook.find(getBundleContext(2), null, null, true, this.serviceReferences);
        assertServiceReferencePresent(0, 1, 2);
    }


    private void assertServiceReferencePresent(int... indices) {
        assertServiceReferencePresent(this.serviceReferences, indices);
    }

}
