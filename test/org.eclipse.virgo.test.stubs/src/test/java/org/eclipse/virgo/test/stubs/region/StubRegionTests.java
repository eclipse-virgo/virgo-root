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

import static org.junit.Assert.assertEquals;

import org.eclipse.equinox.region.Region;
import org.junit.Test;

public class StubRegionTests {

	private StubRegionDigraph stubRegionDigraph = new StubRegionDigraph();
	
	@Test
	public void testRegion(){
		Region region = new StubRegion("testRegion", this.stubRegionDigraph);
		assertEquals("testRegion", region.getName());
	}
}
