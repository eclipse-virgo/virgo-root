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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.equinox.region.RegionDigraph.FilteredRegion;
import org.eclipse.equinox.region.RegionDigraphVisitor;
import org.eclipse.equinox.region.RegionFilter;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * {@link GlobalRegion} is an implementation of {@link Region} which acts as a place holder for all artifacts that are not bundles
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class GlobalRegion implements Region {

    private final String regionName;
    
    private static final String UNSUPPORTED_OPERATION_MESSAGE = "Bundle operations are not support on the Independent Region";

    
    public GlobalRegion(String regionName) {
        this.regionName = regionName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return regionName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBundle(Bundle bundle) throws BundleException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBundle(long bundleId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle installBundle(String location) throws BundleException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle(@NonNull String symbolicName, @NonNull Version version) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectRegion(Region headRegion, RegionFilter filter) throws BundleException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionDigraph getRegionDigraph(){
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Bundle bundle) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(long bundleId) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBundle(Bundle bundle) {
        //no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBundle(long bundleId) {
        //no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Long> getBundleIds() {
        return new HashSet<Long>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<FilteredRegion> getEdges() {
        return new HashSet<RegionDigraph.FilteredRegion>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitSubgraph(RegionDigraphVisitor visitor) {
        //no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + regionName.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GlobalRegion)) {
            return false;
        }
        GlobalRegion other = (GlobalRegion) obj;
        return this.regionName.equals(other.regionName);
    }
    
	@Override
	public Bundle installBundleAtLocation(String arg0, InputStream arg1)
			throws BundleException {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

}
