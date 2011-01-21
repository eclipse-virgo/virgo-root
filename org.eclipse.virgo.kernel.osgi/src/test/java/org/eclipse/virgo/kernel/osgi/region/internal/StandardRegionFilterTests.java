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

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Set;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.util.math.OrderedPair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.Version;

public class StandardRegionFilterTests {

    private static final String PACKAGE = "p";

    private static final String BUNDLE_SYMBOLIC_NAME = "A";

    private static final Version BUNDLE_VERSION = new Version("0");

    private RegionFilter regionFilter;

    private Filter mockFilter;

    @Before
    public void setUp() throws Exception {
        this.regionFilter = new StandardRegionFilter();
        mockFilter = EasyMock.createMock(Filter.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAllowBundle() {
        this.regionFilter.allowBundle(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);
        Set<OrderedPair<String, Version>> allowedBundles = this.regionFilter.getAllowedBundles();
        assertEquals(1, allowedBundles.size());
        assertTrue(allowedBundles.contains(new OrderedPair<String, Version>(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION)));
    }

    @Test
    public void testAllowPackage() {
        this.regionFilter.allowPackage(PACKAGE);
        Collection<String> allowedPackages = this.regionFilter.getAllowedPackages();
        assertEquals(1, allowedPackages.size());
        assertTrue(allowedPackages.contains(PACKAGE));
    }

    @Test
    public void testAllowPackageStem() {
        this.regionFilter.allowPackageStem(PACKAGE);
        Collection<String> allowedPackageStems = this.regionFilter.getAllowedPackageStems();
        assertEquals(1, allowedPackageStems.size());
        assertTrue(allowedPackageStems.contains(PACKAGE));
    }

    @Test
    public void testSetServiceFilter() {
        this.regionFilter.setServiceFilter(this.mockFilter);
        assertEquals(this.mockFilter, this.regionFilter.getServiceFilter());
    }

}
