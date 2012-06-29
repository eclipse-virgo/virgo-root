/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 copyright_holder
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    cgfrost - initial contribution
 */

package org.eclipse.virgo.kernel.deployer.test;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.equinox.region.RegionDigraphVisitor;
import org.eclipse.equinox.region.RegionFilter;
import org.eclipse.equinox.region.RegionDigraph.FilteredRegion;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * StubRegion
 */
public class StubRegion implements Region {

    private final String name;

    public StubRegion(String name) {
        this.name = name;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void addBundle(Bundle bundle) throws BundleException {

    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void addBundle(long bundleId) {

    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Bundle installBundle(String location) throws BundleException {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Set<Long> getBundleIds() {
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
    public Bundle getBundle(String symbolicName, Version version) {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void connectRegion(Region headRegion, RegionFilter filter) throws BundleException {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public RegionDigraph getRegionDigraph() {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void removeBundle(Bundle bundle) {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void removeBundle(long bundleId) {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Set<FilteredRegion> getEdges() {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void visitSubgraph(RegionDigraphVisitor visitor) {
    }

	@Override
	public Bundle installBundleAtLocation(String arg0, InputStream arg1)
			throws BundleException {
		return null;
	}

}
