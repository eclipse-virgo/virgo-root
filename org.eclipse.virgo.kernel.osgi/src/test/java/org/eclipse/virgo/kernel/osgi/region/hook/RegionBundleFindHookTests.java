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

package org.eclipse.virgo.kernel.osgi.region.hook;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.kernel.osgi.region.internal.BundleIdBasedRegion;
import org.eclipse.virgo.kernel.osgi.region.internal.StandardRegionDigraph;
import org.eclipse.virgo.kernel.osgi.region.internal.StandardRegionFilter;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.util.math.OrderedPair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.Version;
import org.osgi.framework.hooks.bundle.FindHook;

public class RegionBundleFindHookTests {

    private static final Version BUNDLE_VERSION = new Version("0");

    private long bundleId;

    private static final String REGION_A = "RegionA";

    private static final String BUNDLE_A = "BundleA";

    private static final String REGION_B = "RegionB";

    private static final String BUNDLE_B = "BundleB";

    private static final String REGION_C = "RegionC";

    private static final String BUNDLE_C = "BundleC";

    private static final String REGION_D = "RegionD";

    private static final String BUNDLE_D = "BundleD";

    private StandardRegionDigraph digraph;

    private StubBundleContext stubBundleContext;

    private FindHook bundleFindHook;

    private Map<String, Region> regions;

    private Map<String, Bundle> bundles;

    private Collection<Bundle> candidates;

    @Before
    public void setUp() throws Exception {
        this.bundleId = 1L;
        this.regions = new HashMap<String, Region>();
        this.bundles = new HashMap<String, Bundle>();
        this.digraph = new StandardRegionDigraph();
        this.stubBundleContext = new StubBundleContext();
        this.bundleFindHook = new RegionBundleFindHook(this.digraph);
        this.candidates = new HashSet<Bundle>();

        // Create regions A, B, C, D containing bundles A, B, C, D, respectively.
        createRegion(REGION_A, BUNDLE_A);
        createRegion(REGION_B, BUNDLE_B);
        createRegion(REGION_C, BUNDLE_C);
        createRegion(REGION_D, BUNDLE_D);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFindInSameRegion() {
        this.candidates.add(bundle(BUNDLE_A));
        this.bundleFindHook.find(bundleContext(BUNDLE_A), this.candidates);
        assertTrue(this.candidates.contains(bundle(BUNDLE_A)));
    }

    @Test
    public void testFindInDisconnectedRegion() {
        this.candidates.add(bundle(BUNDLE_B));
        this.bundleFindHook.find(bundleContext(BUNDLE_A), this.candidates);
        assertFalse(this.candidates.contains(bundle(BUNDLE_B)));
    }

    @Test
    public void testFindConnectedRegionAllowed() throws BundleException {
        RegionFilter filter = createFilter(BUNDLE_B);
        region(REGION_A).connectRegion(region(REGION_B), filter);

        this.candidates.add(bundle(BUNDLE_B));
        this.bundleFindHook.find(bundleContext(BUNDLE_A), this.candidates);
        assertTrue(this.candidates.contains(bundle(BUNDLE_B)));
    }

    @Test
    public void testFindConnectedRegionFiltering() throws BundleException {
        region(REGION_A).connectRegion(region(REGION_B), createFilter(BUNDLE_B));
        Bundle x = createBundle("X");
        region(REGION_B).addBundle(x);

        this.candidates.add(bundle(BUNDLE_B));
        this.candidates.add(x);
        this.bundleFindHook.find(bundleContext(BUNDLE_A), this.candidates);
        assertTrue(this.candidates.contains(bundle(BUNDLE_B)));
        assertFalse(this.candidates.contains(x));
    }

    @Test
    public void testFindTransitive() throws BundleException {
        region(REGION_A).connectRegion(region(REGION_B), createFilter(BUNDLE_C));
        region(REGION_B).connectRegion(region(REGION_C), createFilter(BUNDLE_C));
        Bundle x = createBundle("X");
        region(REGION_C).addBundle(x);

        this.candidates.add(bundle(BUNDLE_B));
        this.candidates.add(bundle(BUNDLE_C));
        this.bundleFindHook.find(bundleContext(BUNDLE_A), this.candidates);
        assertTrue(this.candidates.contains(bundle(BUNDLE_C)));
        assertFalse(this.candidates.contains(bundle(BUNDLE_B)));
        assertFalse(this.candidates.contains(x));

    }

    private Region createRegion(String regionName, String... bundleSymbolicNames) throws BundleException {
        Region region = new BundleIdBasedRegion(regionName, this.digraph, this.stubBundleContext);
        for (String bundleSymbolicName : bundleSymbolicNames) {
            Bundle stubBundle = createBundle(bundleSymbolicName);
            region.addBundle(stubBundle);
        }
        this.regions.put(regionName, region);
        this.digraph.addRegion(region);
        return region;
    }

    private Region region(String regionName) {
        return this.regions.get(regionName);
    }

    private RegionFilter createFilter(String... bundleSymbolicNames) {
        RegionFilter filter = new StandardRegionFilter();
        for (String bundleSymbolicName : bundleSymbolicNames) {
            filter.allowBundle(bundleSymbolicName, BUNDLE_VERSION);
        }
        return filter;
    }
    
    private Bundle createBundle(String bundleSymbolicName) {
        Bundle stubBundle = new StubBundle(this.bundleId++, bundleSymbolicName, BUNDLE_VERSION, "loc:" + bundleSymbolicName);
        this.bundles.put(bundleSymbolicName, stubBundle);
        return stubBundle;
    }

    private BundleContext bundleContext(String bundleSymbolicName) {
        return bundle(bundleSymbolicName).getBundleContext();
    }

    private Bundle bundle(String bundleSymbolicName) {
        Bundle bundleA = this.bundles.get(bundleSymbolicName);
        return bundleA;
    }

}
