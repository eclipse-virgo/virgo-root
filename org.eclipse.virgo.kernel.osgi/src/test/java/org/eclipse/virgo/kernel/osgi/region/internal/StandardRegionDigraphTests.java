/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2011 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region.internal;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.eclipse.virgo.kernel.osgi.region.internal.StandardRegionDigraph;
import org.eclipse.virgo.util.math.OrderedPair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

public class StandardRegionDigraphTests {

    private RegionDigraph digraph;

    private Region mockRegion1;

    private Region mockRegion2;

    private Region mockRegion3;

    private RegionFilter regionFilter1;

    private RegionFilter regionFilter2;

    private Bundle mockBundle;

    @Before
    public void setUp() throws Exception {
        this.digraph = new StandardRegionDigraph();
        this.mockRegion1 = EasyMock.createMock(Region.class);
        this.mockRegion2 = EasyMock.createMock(Region.class);
        this.mockRegion3 = EasyMock.createMock(Region.class);
        this.regionFilter1 = EasyMock.createMock(RegionFilter.class);
        this.regionFilter2 = EasyMock.createMock(RegionFilter.class);
        this.mockBundle = EasyMock.createMock(Bundle.class);
    }

    private void setDefaultMockFilters() {
        setMockFilterAllowedBundles(this.regionFilter1);
        setMockFilterAllowedBundles(this.regionFilter2);
    }

    private void setMockFilterAllowedBundles(RegionFilter regionFilter, OrderedPair<String, Version>... bundles) {
        EasyMock.expect(regionFilter.getAllowedBundles()).andReturn(new HashSet<OrderedPair<String, Version>>(Arrays.asList(bundles))).anyTimes();
    }

    private void replayMocks() {
        EasyMock.replay(this.mockRegion1, this.mockRegion2, this.mockRegion3, this.regionFilter1, this.regionFilter2, this.mockBundle);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(this.mockRegion1, this.mockRegion2, this.mockRegion3, this.regionFilter1, this.regionFilter2, this.mockBundle);
    }

    @Test
    public void testConnect() throws BundleException {
        setDefaultMockFilters();
        replayMocks();
        
        this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConnectWithFilterContents() throws BundleException {
        OrderedPair<String, Version> b1 = new OrderedPair<String,Version>("b1", new Version("0"));
        setMockFilterAllowedBundles(this.regionFilter1, b1);
        EasyMock.expect(this.mockRegion1.getBundle(b1.getFirst(), b1.getSecond())).andReturn(null).anyTimes();
        
        OrderedPair<String, Version> b2 = new OrderedPair<String,Version>("b2", new Version("0"));
        setMockFilterAllowedBundles(this.regionFilter2, b2);
        EasyMock.expect(this.mockRegion1.getBundle(b2.getFirst(), b2.getSecond())).andReturn(null).anyTimes();

        replayMocks();
        
        this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
        this.digraph.connect(this.mockRegion1, this.regionFilter2, this.mockRegion3);
    }

    
    @Test(expected = BundleException.class)
    public void testConnectLoop() throws BundleException {
        setDefaultMockFilters();
        replayMocks();
        
        this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion1);
    }

    @Test(expected = BundleException.class)
    public void testDuplicateConnection() throws BundleException {
        setDefaultMockFilters();
        replayMocks();
        
        this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
        this.digraph.connect(this.mockRegion1, this.regionFilter2, this.mockRegion2);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BundleException.class)
    public void testConnectWithClashingFilters() throws BundleException {
        OrderedPair<String, Version> bundle = new OrderedPair<String,Version>("b", new Version("0"));
        setMockFilterAllowedBundles(this.regionFilter1, bundle);
        setMockFilterAllowedBundles(this.regionFilter2, bundle);
        EasyMock.expect(this.mockRegion1.getBundle(bundle.getFirst(), bundle.getSecond())).andReturn(null).anyTimes();
        replayMocks();
        
        this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
        this.digraph.connect(this.mockRegion1, this.regionFilter2, this.mockRegion3);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BundleException.class)
    public void testConnectWithClashingRegion() throws BundleException {
        OrderedPair<String, Version> bundle = new OrderedPair<String,Version>("b", new Version("0"));
        setMockFilterAllowedBundles(this.regionFilter1, bundle);
        EasyMock.expect(this.mockRegion1.getBundle(bundle.getFirst(), bundle.getSecond())).andReturn(this.mockBundle);
        replayMocks();
        
        this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
    }

 
    @Test
    public void testAddRegion() {
        setDefaultMockFilters();
        replayMocks();
        
        this.digraph.addRegion(this.mockRegion1);
        boolean found = false;
        for (Region region : this.digraph) {
            if (this.mockRegion1.equals(region)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testGetEdges() throws BundleException {
        setDefaultMockFilters();
        replayMocks();
        
        this.digraph.connect(this.mockRegion1, this.regionFilter1, this.mockRegion2);
        this.digraph.connect(this.mockRegion1, this.regionFilter2, this.mockRegion3);
        this.digraph.connect(this.mockRegion2, this.regionFilter2, this.mockRegion1);

        Set<FilteredRegion> edges = this.digraph.getEdges(this.mockRegion1);

        assertEquals(2, edges.size());

        for (FilteredRegion edge : edges) {
            if (edge.getRegion().equals(this.mockRegion2)) {
                assertEquals(this.regionFilter1, edge.getFilter());
            } else if (edge.getRegion().equals(this.mockRegion3)) {
                assertEquals(this.regionFilter2, edge.getFilter());
            } else {
                fail("unexpected edge");
            }
        }
    }

}
