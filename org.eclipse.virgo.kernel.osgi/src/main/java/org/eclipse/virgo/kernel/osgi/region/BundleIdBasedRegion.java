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

import java.io.InputStream;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.eclipse.virgo.util.math.ConcurrentHashSet;
import org.eclipse.virgo.util.math.OrderedPair;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * {@link BundleIdBasedRegion} is an implementation of {@link Region} which keeps a track of the bundles in the region
 * by recording their bundle identifiers.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class BundleIdBasedRegion implements Region {

    private final Set<Long> bundleIds = new ConcurrentHashSet<Long>();

    private final String regionName;

    private final RegionMembership regionMembership;

    private final RegionDigraph regionDigraph;

    BundleIdBasedRegion(String regionName, RegionMembership regionMembership, RegionDigraph regionDigraph) {
        this.regionName = regionName;
        this.regionMembership = regionMembership;
        this.regionDigraph = regionDigraph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.regionName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBundle(Bundle bundle) throws BundleException {
        checkBundleNotAssociatedWithAnotherRegion(bundle);

        String symbolicName = bundle.getSymbolicName();
        Version version = bundle.getVersion();

        checkDuplicateBundleInRegion(bundle, symbolicName, version);

        checkDuplicateBundleViaFilter(bundle, symbolicName, version);

        this.bundleIds.add(bundle.getBundleId());

    }

    private void checkBundleNotAssociatedWithAnotherRegion(Bundle bundle) throws BundleException {
        try {
            Region region = this.regionMembership.getRegion(bundle);
            if (!this.equals(region)) {
                throw new BundleException("Bundle '" + bundle + "' is already associated with region '" + region + "'",
                    BundleException.INVALID_OPERATION);
            }
        } catch (IndeterminateRegionException _) {
            // Normal case
        }
    }

    private void checkDuplicateBundleInRegion(Bundle bundle, String symbolicName, Version version) throws BundleException {
        Bundle existingBundle = this.getBundle(symbolicName, version);
        if (existingBundle != null) {
            throw new BundleException("Cannot add bundle '" + bundle + "' to region '" + this
                + "' as its symbolic name and version conflict with those of bundle '" + existingBundle + "' which is already present in the region",
                BundleException.DUPLICATE_BUNDLE_ERROR);
        }
    }

    private void checkDuplicateBundleViaFilter(Bundle bundle, String symbolicName, Version version) throws BundleException {
        for (FilteredRegion filteredRegion : this.regionDigraph.getEdges(this)) {
            RegionFilter filter = filteredRegion.getFilter();
            if (filter.getAllowedBundles().contains(new OrderedPair<String, Version>(symbolicName, version))) {
                throw new BundleException("Cannot add bundle '" + bundle + "' to region '" + this + "' as filter '" + filter
                    + "' allows it to be seen from region '" + filteredRegion.getRegion() + "'", BundleException.DUPLICATE_BUNDLE_ERROR);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle installBundle(String location) throws BundleException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle(String symbolicName, Version version) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectRegion(Region targetRegion, RegionFilter filter) throws BundleException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleContext getBundleContext() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionPackageImportPolicy getRegionPackageImportPolicy() {
        // TODO Auto-generated method stub
        return null;
    }

}
