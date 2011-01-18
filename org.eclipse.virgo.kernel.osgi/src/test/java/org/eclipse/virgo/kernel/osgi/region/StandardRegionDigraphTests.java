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

package org.eclipse.virgo.kernel.osgi.region;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.eclipse.virgo.util.math.OrderedPair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

public class StandardRegionDigraphTests {

    private static final HashSet<OrderedPair<String, Version>> EMPTY_BUNDLE_SET = new HashSet<OrderedPair<String, Version>>();

    private RegionDigraph digraph;

    private Region mockRegion1;

    private Region mockRegion2;

    private Region mockRegion3;

    private RegionFilter regionFilter1;

    private RegionFilter regionFilter2;

    @Before
    public void setUp() throws Exception {
        this.digraph = new StandardRegionDigraph();
        this.mockRegion1 = EasyMock.createMock(Region.class);
        this.mockRegion2 = EasyMock.createMock(Region.class);
        this.mockRegion3 = EasyMock.createMock(Region.class);
        this.regionFilter1 = EasyMock.createMock(RegionFilter.class);
        this.regionFilter2 = EasyMock.createMock(RegionFilter.class);
        EasyMock.expect(this.regionFilter1.getAllowedBundles()).andReturn(EMPTY_BUNDLE_SET).anyTimes();
        EasyMock.expect(this.regionFilter2.getAllowedBundles()).andReturn(EMPTY_BUNDLE_SET).anyTimes();
        EasyMock.replay(this.mockRegion1, this.mockRegion2, this.mockRegion3, this.regionFilter1, this.regionFilter2);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(this.mockRegion1, this.mockRegion2, this.mockRegion3, this.regionFilter1, this.regionFilter2);
    }

    @Test
    public void testConnect() throws BundleException {
        this.digraph.connect(this.mockRegion1, this.mockRegion2, this.regionFilter1);
    }

    @Test(expected = BundleException.class)
    public void testConnectLoop() throws BundleException {
        this.digraph.connect(this.mockRegion1, this.mockRegion1, this.regionFilter1);
    }
    
    @Test(expected = BundleException.class)
    public void testDuplicateConnection() throws BundleException {
        this.digraph.connect(this.mockRegion1, this.mockRegion2, this.regionFilter1);
        this.digraph.connect(this.mockRegion1, this.mockRegion2, this.regionFilter2);
    }


    @Test
    public void testAddRegion() {
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
        this.digraph.connect(this.mockRegion1, this.mockRegion2, this.regionFilter1);
        this.digraph.connect(this.mockRegion1, this.mockRegion3, this.regionFilter2);
        this.digraph.connect(this.mockRegion2, this.mockRegion1, this.regionFilter2);
        
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
