/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.test.stubs.region;

import org.eclipse.equinox.region.Region;
import org.junit.Test;
import org.osgi.framework.BundleException;

import static org.junit.Assert.assertEquals;


public class StubRegionDigraphTests {

	private StubRegionDigraph stubRegionDigraph;
	
	@Test
	public void testRegionDigraaph() throws BundleException{
		this.stubRegionDigraph = new StubRegionDigraph();
		Region createRegion = this.stubRegionDigraph.createRegion("testRegion");
		assertEquals(createRegion.getName(), "testRegion");
		assertEquals(this.stubRegionDigraph, createRegion.getRegionDigraph());
	}
	
	@Test
	public void testGetRemoveRegion() throws BundleException {
		this.stubRegionDigraph = new StubRegionDigraph();
		this.stubRegionDigraph.createRegion("testRegion");
		assertEquals(1, this.stubRegionDigraph.getRegions().size());
		this.stubRegionDigraph.removeRegion(this.stubRegionDigraph.getRegion("testRegion"));
		assertEquals(0, this.stubRegionDigraph.getRegions().size());
	}
	
}
