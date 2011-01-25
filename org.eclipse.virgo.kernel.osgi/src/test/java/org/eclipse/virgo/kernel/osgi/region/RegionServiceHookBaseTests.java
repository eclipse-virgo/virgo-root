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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class RegionServiceHookBaseTests extends AbstractRegionServiceHookTest {

    private TestRegionServiceHook regionServiceHook;
    
    private Region kernelRegion;

    @Before
    public void setUp() {
        this.regionServiceHook = new TestRegionServiceHook(getRegionMembership(), "", "");
        this.kernelRegion = getKernelRegion();
    }

    @Test
    public void testIsUserRegionService() {
        assertNull(this.regionServiceHook.getRegion(getServiceReference(SYSTEM_BUNDLE_INDEX)));
        assertEquals(this.kernelRegion, this.regionServiceHook.getRegion(getServiceReference(KERNEL_BUNDLE_INDEX)));
        assertFalse(this.kernelRegion.equals(this.regionServiceHook.getRegion(getServiceReference(USER_REGION_BUNDLE_INDEX))));
    }

    @Test
    public void testIsSystemBundleService() {
        assertTrue(RegionServiceHookBase.isSystemBundleService(getServiceReference(SYSTEM_BUNDLE_INDEX)));
        assertFalse(RegionServiceHookBase.isSystemBundleService(getServiceReference(KERNEL_BUNDLE_INDEX)));
    }

    @Test
    public void testServiceExported() {
        assertFalse(this.regionServiceHook.serviceExported(getServiceReference(SYSTEM_BUNDLE_INDEX)));
        assertFalse(this.regionServiceHook.serviceExported(getServiceReference(USER_REGION_BUNDLE_INDEX)));
    }

    @Test
    public void testServiceExportedWithExport() {
        this.regionServiceHook = new TestRegionServiceHook(getRegionMembership(), "", "java.lang.String");
        assertTrue(this.regionServiceHook.serviceExported(getServiceReference(SYSTEM_BUNDLE_INDEX)));
        assertTrue(this.regionServiceHook.serviceExported(getServiceReference(USER_REGION_BUNDLE_INDEX)));
    }

    @Test
    public void testServiceImported() {
        assertFalse(this.regionServiceHook.serviceExported(getServiceReference(SYSTEM_BUNDLE_INDEX)));
        assertFalse(this.regionServiceHook.serviceExported(getServiceReference(KERNEL_BUNDLE_INDEX)));
    }

    @Test
    public void testServiceImportedWithImports() {
        this.regionServiceHook = new TestRegionServiceHook(getRegionMembership(), "java.lang.String", "");
        assertTrue(this.regionServiceHook.serviceImported(getServiceReference(SYSTEM_BUNDLE_INDEX)));
        assertTrue(this.regionServiceHook.serviceImported(getServiceReference(KERNEL_BUNDLE_INDEX)));
    }

    private class TestRegionServiceHook extends RegionServiceHookBase {

        TestRegionServiceHook(RegionMembership regionMembership, String regionServiceImports, String regionServiceExports) {
            super(regionMembership, regionServiceImports, regionServiceExports);
        }

    }
}
