/**
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
package org.eclipse.virgo.test.stubs.region;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.equinox.region.RegionDigraphPersistence;
import org.eclipse.equinox.region.RegionDigraphVisitor;
import org.eclipse.equinox.region.RegionFilter;
import org.eclipse.equinox.region.RegionFilterBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.hooks.bundle.EventHook;
import org.osgi.framework.hooks.bundle.FindHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;

public class StubRegionDigraph implements RegionDigraph {

	private final Map<String, Region> regions = new HashMap<String, Region>();
	
	private Region defaultRegion;
	
	@Override
	public Iterator<Region> iterator() {
		return this.regions.values().iterator();
	}

	@Override
	public Region createRegion(String regionName) throws BundleException {
		StubRegion stubRegion = new StubRegion(regionName, this);
		this.regions.put(regionName, stubRegion);
		return stubRegion;
	}

	@Override
	public RegionFilterBuilder createRegionFilterBuilder() {
		return null;
	}

	@Override
	public void removeRegion(Region region) {
		this.regions.remove(region.getName());
	}

	@Override
	public Set<Region> getRegions() {
		return Collections.unmodifiableSet(new HashSet<Region>(this.regions.values()));
	}

	@Override
	public Region getRegion(String regionName) {
		return this.regions.get(regionName);
	}

	@Override
	public Region getRegion(Bundle bundle) {
		return null;
	}

	@Override
	public Region getRegion(long bundleId) {
		return null;
	}

	@Override
	public void connect(Region tailRegion, RegionFilter filter, Region headRegion) throws BundleException {

	}

	@Override
	public Set<FilteredRegion> getEdges(Region tailRegion) {
		return null;
	}

	@Override
	public void visitSubgraph(Region startingRegion, RegionDigraphVisitor visitor) {

	}

	@Override
	public RegionDigraphPersistence getRegionDigraphPersistence() {
		return null;
	}

	@Override
	public RegionDigraph copy() throws BundleException {
		return null;
	}

	@Override
	public void replace(RegionDigraph digraph) throws BundleException {

	}

	@Override
	public ResolverHookFactory getResolverHookFactory() {
		return null;
	}

	@Override
	public EventHook getBundleEventHook() {
		return null;
	}

	@Override
	public FindHook getBundleFindHook() {
		return null;
	}

	@Override
	public org.osgi.framework.hooks.service.EventHook getServiceEventHook() {
		return null;
	}

	@Override
	public org.osgi.framework.hooks.service.FindHook getServiceFindHook() {
		return null;
	}

	@Override
	public void setDefaultRegion(Region defaultRegion) {
		this.defaultRegion = defaultRegion;
	}

	@Override
	public Region getDefaultRegion() {
		return this.defaultRegion;
	}

}
