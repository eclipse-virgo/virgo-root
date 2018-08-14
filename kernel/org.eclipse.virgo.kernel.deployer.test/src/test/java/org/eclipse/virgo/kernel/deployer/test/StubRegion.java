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

public class StubRegion implements Region {

    private final String name;

    public StubRegion(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void addBundle(Bundle bundle) throws BundleException {

    }

    @Override
    public void addBundle(long bundleId) {

    }

    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        return null;
    }

    @Override
    public Bundle installBundle(String location) throws BundleException {
        return null;
    }

    @Override
    public Set<Long> getBundleIds() {
        return null;
    }

    @Override
    public boolean contains(Bundle bundle) {
        return false;
    }

    @Override
    public boolean contains(long bundleId) {
        return false;
    }

    @Override
    public Bundle getBundle(String symbolicName, Version version) {
        return null;
    }

    @Override
    public void connectRegion(Region headRegion, RegionFilter filter) throws BundleException {
    }

    @Override
    public RegionDigraph getRegionDigraph() {
        return null;
    }

    @Override
    public void removeBundle(Bundle bundle) {
    }

    @Override
    public void removeBundle(long bundleId) {
    }

    @Override
    public Set<FilteredRegion> getEdges() {
        return null;
    }

    @Override
    public void visitSubgraph(RegionDigraphVisitor visitor) {
    }

	@Override
	public Bundle installBundleAtLocation(String arg0, InputStream arg1) {
		return null;
	}

}
