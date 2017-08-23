/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.test.stubs.region.StubRegionDigraph;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

/**
 */
public class StandardQuasiRequiredBundleTests {
    
    private static final String STRING_VERSION_RANGE = "[2,4)";

    private static final org.eclipse.osgi.service.resolver.VersionRange RESOLVER_VERSION_RANGE = new org.eclipse.osgi.service.resolver.VersionRange(STRING_VERSION_RANGE);
    
    private static final VersionRange VERSION_RANGE = new VersionRange(STRING_VERSION_RANGE);

    private static final String BSN = "bsn";
    
    private static final String REQUIRED_BSN = "required-bsn";
    
    private StubBundleDescription bundleDescription;

    private QuasiBundle qb;

    private StubBundleSpecification bundleSpecification;
    
    private Region stubRegion;

    @Before
    public void setUp() throws BundleException {
        this.bundleDescription = new StubBundleDescription();
        this.bundleDescription.setBundleSymbolicName(BSN);
        this.bundleSpecification = new StubBundleSpecification(REQUIRED_BSN);
        this.stubRegion = new StubRegionDigraph().createRegion("testRegion");
        this.qb = new StandardQuasiBundle(this.bundleDescription, null, this.stubRegion);
    }

    @Test
    public void testRequiredBundleName() {
        QuasiRequiredBundle qrb = new StandardQuasiRequiredBundle(this.bundleSpecification, this.qb);
        Assert.assertEquals(REQUIRED_BSN, qrb.getRequiredBundleName());
    }
    
    @Test
    public void testVersionConstraint() {
        this.bundleSpecification.setVersionRange(RESOLVER_VERSION_RANGE);
        QuasiRequiredBundle qrb = new StandardQuasiRequiredBundle(this.bundleSpecification, this.qb);
        Assert.assertEquals(VERSION_RANGE, qrb.getVersionConstraint());
    }
    
    @Test
    public void testResolved() {
        this.bundleSpecification.setResolved(true);
        QuasiRequiredBundle qrb = new StandardQuasiRequiredBundle(this.bundleSpecification, this.qb);
        Assert.assertTrue(qrb.isResolved());
    }
    
    @Test
    public void testUnresolved() {
        QuasiRequiredBundle qrb = new StandardQuasiRequiredBundle(this.bundleSpecification, this.qb);
        Assert.assertFalse(qrb.isResolved());
    }
    
    @Test
    public void testUnresolvedProvider() {
        QuasiRequiredBundle qrb = new StandardQuasiRequiredBundle(this.bundleSpecification, this.qb);
        Assert.assertNull(qrb.getProvider());
    }
    
    @Test
    public void testResolvedProvider() {
        StubBundleDescription requiredBundleDescription = new StubBundleDescription();
        requiredBundleDescription.setBundleSymbolicName(REQUIRED_BSN);
        QuasiBundle rqb = new StandardQuasiBundle(requiredBundleDescription, null, null);
        QuasiRequiredBundle qrb = new StandardQuasiRequiredBundle(this.bundleSpecification, this.qb);
        this.bundleSpecification.setResolved(true);
        StubBaseDescription supplier = new StubBaseDescription();
        supplier.setSupplier(requiredBundleDescription);
        this.bundleSpecification.setSupplier(supplier);
        Assert.assertEquals(rqb, qrb.getProvider());
    }
    
    @Test
    public void testRequiringBundle() {
        QuasiRequiredBundle qrb = new StandardQuasiRequiredBundle(this.bundleSpecification, this.qb);
        Assert.assertEquals(this.qb, qrb.getRequiringBundle());
    }

}
