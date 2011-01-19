/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

public class BundleIdBasedRegionTests {

    private static final String OTHER_REGION_NAME = "other";

    private static final String BUNDLE_SYMBOLIC_NAME = "b";

    private static final Version BUNDLE_VERSION = new Version("1");

    private static final long BUNDLE_ID = 1L;

    private static final String REGION_NAME = "reg";

    private Bundle mockBundle;

    private RegionDigraph mockGraph;

    private Iterator<Region> regionIterator;

    private BundleContext mockBundleContext;

    private Region mockRegion;

    private RegionFilter mockRegionFilter;

    @Before
    public void setUp() throws Exception {
        this.mockBundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(this.mockBundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME).anyTimes();
        EasyMock.expect(this.mockBundle.getVersion()).andReturn(BUNDLE_VERSION).anyTimes();
        EasyMock.expect(this.mockBundle.getBundleId()).andReturn(BUNDLE_ID).anyTimes();
        
        this.mockBundleContext = EasyMock.createMock(BundleContext.class);
        EasyMock.expect(this.mockBundleContext.getBundle(BUNDLE_ID)).andReturn(this.mockBundle).anyTimes();
        
        this.mockRegion = EasyMock.createMock(Region.class);
        
        this.mockRegionFilter = EasyMock.createMock(RegionFilter.class);

        this.regionIterator = new Iterator<Region>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Region next() {
                return null;
            }

            @Override
            public void remove() {
            }
        };
        this.mockGraph = EasyMock.createMock(RegionDigraph.class);
        EasyMock.expect(this.mockGraph.iterator()).andReturn(this.regionIterator).anyTimes();
        EasyMock.expect(this.mockGraph.getEdges(EasyMock.isA(Region.class))).andReturn(new HashSet<FilteredRegion>()).anyTimes();
        this.mockGraph.connect(EasyMock.isA(Region.class), EasyMock.eq(this.mockRegion), EasyMock.eq(this.mockRegionFilter));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(this.mockBundleContext, this.mockBundle, this.mockRegion, this.mockGraph);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(this.mockBundleContext, this.mockBundle, this.mockRegion, this.mockGraph);
    }

    @Test
    public void testGetName() {
        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        assertEquals(REGION_NAME, r.getName());
    }

    @Test
    public void testAddBundle() throws BundleException {
        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
    }

    @Test
    public void testInstallBundleStringInputStream() {
        // TODO
    }

    @Test
    public void testInstallBundleString() {
        // TODO
    }

    @Test
    public void testGetBundle() throws BundleException {
        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.addBundle(this.mockBundle);
        assertEquals(this.mockBundle, r.getBundle(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION));
    }

    @Test
    public void testConnectRegion() throws BundleException {
        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        r.connectRegion(this.mockRegion, this.mockRegionFilter);
    }
    
    @Test
    public void testEquals() {
        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        Region s = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        assertEquals(r,s);
        assertEquals(r.hashCode(), s.hashCode());
    }
    
    @Test
    public void testNotEqual() {
        Region r = new BundleIdBasedRegion(REGION_NAME, this.mockGraph, this.mockBundleContext);
        Region s = new BundleIdBasedRegion(OTHER_REGION_NAME, this.mockGraph, this.mockBundleContext);
        assertFalse(r.equals(s));
    }

}
