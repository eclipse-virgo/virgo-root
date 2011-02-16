/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    VMware Inc. - initial contribution
 */

package org.eclipse.virgo.kernel.osgi.region.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.kernel.osgi.region.RegionPackageImportPolicy;
import org.eclipse.virgo.kernel.osgi.region.StandardRegionFilter;
import org.eclipse.virgo.util.osgi.VersionRange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.Version;

public class StandardRegionFilterTests {

    private static final String BUNDLE_SYMBOLIC_NAME = "A";

    private static final Version BUNDLE_VERSION = new Version("0");

    private RegionFilter regionFilter;

    private Filter mockFilter;

    private RegionPackageImportPolicy packageImportPolicy;

    @Before
    public void setUp() throws Exception {
        this.regionFilter = new StandardRegionFilter();
        this.mockFilter = EasyMock.createMock(Filter.class);
        this.packageImportPolicy = EasyMock.createMock(RegionPackageImportPolicy.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAllowBundle() {
        this.regionFilter.allowBundle(BUNDLE_SYMBOLIC_NAME, VersionRange.NATURAL_NUMBER_RANGE);
        assertTrue(this.regionFilter.isBundleAllowed(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
    }
    
    @Test
    public void testBundleNotAllowed() {
        assertFalse(this.regionFilter.isBundleAllowed(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
    }

    @Test
    public void testBundleNotAllowedInRange() {
        this.regionFilter.allowBundle(BUNDLE_SYMBOLIC_NAME, new VersionRange("1"));

        assertFalse(this.regionFilter.isBundleAllowed(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
    }

    @Test
    public void testSetPackageImportPolicy() {
        this.regionFilter.setPackageImportPolicy(this.packageImportPolicy);
        assertEquals(this.packageImportPolicy, this.regionFilter.getPackageImportPolicy());
    }

    @Test
    public void testSetServiceFilter() {
        this.regionFilter.setServiceFilter(this.mockFilter);
        assertEquals(this.mockFilter, this.regionFilter.getServiceFilter());
    }

}
