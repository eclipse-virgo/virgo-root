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

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import static org.junit.Assert.assertEquals;

public class GlobalRegionTests {

    private static final Version TEST_VERSION = Version.emptyVersion;

    private static final String TEST_BUNDLE_SYMBOLIC_NAME = "a";

    private static final String TEST_REGION_NAME = "test.region";

    private Region region;

    @Before
    public void setUp() {
        this.region = new GlobalRegion(TEST_REGION_NAME);
    }

    @Test
    public void testGetName() {
        assertEquals(TEST_REGION_NAME, this.region.getName());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testGetBundle() throws UnsupportedOperationException {
       this.region.getBundle(TEST_BUNDLE_SYMBOLIC_NAME, TEST_VERSION);
    }
    
    @Test(expected=FatalAssertionException.class)
    public void testGetBundleWithNullSymbolicName() throws UnsupportedOperationException {
       this.region.getBundle(null, TEST_VERSION);
    }
    
    @Test(expected=FatalAssertionException.class)
    public void testGetBundleWithNullVersion() throws UnsupportedOperationException {
       this.region.getBundle(TEST_BUNDLE_SYMBOLIC_NAME, null);
    }

    // Other tests could be added in due course, but the point of adding this test was really to pin down bug 341012
}
