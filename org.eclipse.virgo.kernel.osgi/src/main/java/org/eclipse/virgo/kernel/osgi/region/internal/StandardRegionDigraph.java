/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.math.OrderedPair;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * {@link StandardRegionDigraph} is the default implementation of {@link RegionDigraph}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public final class StandardRegionDigraph implements RegionDigraph {

    private final Object monitor = new Object();

    private final Set<Region> regions = new HashSet<Region>();

    private final Map<OrderedPair<Region, Region>, RegionFilter> filter = new HashMap<OrderedPair<Region, Region>, RegionFilter>();

    private final BundleContext systemBundleContext;

    private final ThreadLocal<Region> threadLocal;

    public StandardRegionDigraph(BundleContext bundleContext, ThreadLocal<Region> threadLocal) {
        this.systemBundleContext = bundleContext.getBundle(0L).getBundleContext();
        this.threadLocal = threadLocal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region createRegion(String regionName) throws BundleException {
        Region region = new BundleIdBasedRegion(regionName, this, this.systemBundleContext, this.threadLocal);
        synchronized (this.monitor) {
            if (getRegion(regionName) != null) {
                throw new BundleException("Region '" + regionName + "' already exists", BundleException.UNSUPPORTED_OPERATION);
            }
            this.regions.add(region);
            return region;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(@NonNull Region tailRegion, @NonNull RegionFilter filter, @NonNull Region headRegion) throws BundleException {
        if (headRegion.equals(tailRegion)) {
            throw new BundleException("Cannot connect region '" + headRegion + "' to itself", BundleException.UNSUPPORTED_OPERATION);
        }
        OrderedPair<Region, Region> nodePair = new OrderedPair<Region, Region>(tailRegion, headRegion);
        synchronized (this.monitor) {
            if (this.filter.containsKey(nodePair)) {
                throw new BundleException("Region '" + tailRegion + "' is already connected to region '" + headRegion,
                    BundleException.UNSUPPORTED_OPERATION);
            } else {
                checkFilterDoesNotAllowExistingBundle(tailRegion, filter);
                this.regions.add(tailRegion);
                this.regions.add(headRegion);
                this.filter.put(nodePair, filter);
            }
        }

    }

    private void checkFilterDoesNotAllowExistingBundle(Region tailRegion, RegionFilter filter) throws BundleException {
        // TODO: enumerate the bundles in the region and check the filter does not allow any of them
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Region getRegion(@NonNull String regionName) {
        synchronized (this.monitor) {
            for (Region region : this) {
                if (regionName.equals(region.getName())) {
                    return region;
                }
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region getRegion(Bundle bundle) {
        synchronized (this.monitor) {
            for (Region region : this) {
                if (region.contains(bundle)) {
                    return region;
                }
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Region getRegion(long bundleId) {
        synchronized (this.monitor) {
            for (Region region : this) {
                if (region.contains(bundleId)) {
                    return region;
                }
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRegion(@NonNull Region coregion) {
        synchronized (this.monitor) {
            this.regions.remove(coregion);
            Iterator<OrderedPair<Region, Region>> i = this.filter.keySet().iterator();
            while (i.hasNext()) {
                OrderedPair<Region, Region> regionPair = i.next();
                if (coregion.equals(regionPair.getFirst()) || coregion.equals(regionPair.getSecond())) {
                    i.remove();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        synchronized (this.monitor) {
            StringBuffer s = new StringBuffer();
            boolean first = true;
            s.append("RegionDigraph{");
            for (Region r : this) {
                if (!first) {
                    s.append(", ");
                }
                s.append(r);
                first = false;
            }
            s.append("}");
            s.append("[");
            first = true;
            for (OrderedPair<Region, Region> regionPair : this.filter.keySet()) {
                if (!first) {
                    s.append(", ");
                }
                s.append(regionPair.getFirst() + "->" + regionPair.getSecond());
                first = false;
            }
            s.append("]");
            return s.toString();
        }
    }

}
