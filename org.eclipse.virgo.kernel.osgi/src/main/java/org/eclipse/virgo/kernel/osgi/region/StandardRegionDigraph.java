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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.math.OrderedPair;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * {@link StandardRegionDigraph} is the default implementation of {@link RegionDigraph}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class StandardRegionDigraph implements RegionDigraph {

    private final Object monitor = new Object();

    private final Set<Region> regions = new HashSet<Region>();

    private final Map<OrderedPair<Region, Region>, RegionFilter> filter = new HashMap<OrderedPair<Region, Region>, RegionFilter>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(@NonNull Region tailRegion, @NonNull Region headRegion, @NonNull RegionFilter filter) throws BundleException {
        if (headRegion.equals(tailRegion)) {
            throw new BundleException("Cannot connect region '" + headRegion + "' to itself", BundleException.UNSUPPORTED_OPERATION);
        }
        OrderedPair<Region, Region> nodePair = new OrderedPair<Region, Region>(tailRegion, headRegion);
        synchronized (this.monitor) {
            if (this.filter.containsKey(nodePair)) {
                throw new BundleException("Region '" + tailRegion + "' is already connected to region '" + headRegion,
                    BundleException.UNSUPPORTED_OPERATION);
            } else {
                checkDuplicateBundle(tailRegion, filter);
                this.regions.add(tailRegion);
                this.regions.add(headRegion);
                this.filter.put(nodePair, filter);
            }
        }

    }

    private void checkDuplicateBundle(Region tailRegion, RegionFilter filter) throws BundleException {
        checkFilterDoesNotAllowExistingBundle(tailRegion, filter);
        checkFilterDoesNotAllowSameBundleAsExistingFilter(tailRegion, filter);
    }

    private void checkFilterDoesNotAllowExistingBundle(Region tailRegion, RegionFilter filter) throws BundleException {
        for (OrderedPair<String, Version> filterBundle : filter.getAllowedBundles()) {
            String symbolicName = filterBundle.getFirst();
            Version version = filterBundle.getSecond();
            if (tailRegion.getBundle(symbolicName, version) != null) {
                throw new BundleException("RegionFilter '" + filter + "' allows bundle '" + symbolicName + "_" + version
                    + "' which is present in region '" + tailRegion + "'", BundleException.DUPLICATE_BUNDLE_ERROR);
            }
        }
    }

    private void checkFilterDoesNotAllowSameBundleAsExistingFilter(Region tailRegion, RegionFilter f) throws BundleException {
        Collection<OrderedPair<String, Version>> fAllowed = f.getAllowedBundles();
        for (FilteredRegion edge : getEdges(tailRegion)) {
            RegionFilter g = edge.getFilter();
            Collection<OrderedPair<String, Version>> gAllowed = g.getAllowedBundles();
            for (OrderedPair<String, Version> bundle : gAllowed) {
                if (fAllowed.contains(bundle)) {
                    String symbolicName = bundle.getFirst();
                    Version version = bundle.getSecond();
                    throw new BundleException("RegionFilter '" + f + "' allows bundle '" + symbolicName + "_" + version
                        + "' which is allowed into region '" + tailRegion + "' by filter '" + g + "'", BundleException.DUPLICATE_BUNDLE_ERROR);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Region> iterator() {
        synchronized (this.monitor) {
            Set<Region> snapshot = new HashSet<Region>(this.regions.size());
            snapshot.addAll(this.regions);
            return snapshot.iterator();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRegion(Region region) {
        synchronized (this.monitor) {
            this.regions.add(region);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<FilteredRegion> getEdges(Region tailRegion) {
        Set<FilteredRegion> edges = new HashSet<FilteredRegion>();
        synchronized (this.monitor) {
            Set<OrderedPair<Region, Region>> regionPairs = this.filter.keySet();
            for (OrderedPair<Region, Region> regionPair : regionPairs) {
                if (tailRegion.equals(regionPair.getFirst())) {
                    edges.add(new StandardFilteredRegion(regionPair.getSecond(), this.filter.get(regionPair)));
                }
            }
        }
        return edges;
    }

    private class StandardFilteredRegion implements FilteredRegion {

        private Region region;

        private RegionFilter regionFilter;

        private StandardFilteredRegion(Region region, RegionFilter regionFilter) {
            this.region = region;
            this.regionFilter = regionFilter;
        }

        @Override
        public Region getRegion() {
            return this.region;
        }

        @Override
        public RegionFilter getFilter() {
            return this.regionFilter;
        }

    }

}
