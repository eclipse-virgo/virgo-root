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
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.test.stubs.region.StubRegionDigraph;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

/**
 */
public class StandardQuasiImportPackageTests {

    private static final String STRING_VERSION_RANGE = "[2,4)";

    private static final org.eclipse.osgi.service.resolver.VersionRange RESOLVER_VERSION_RANGE = new org.eclipse.osgi.service.resolver.VersionRange(STRING_VERSION_RANGE);
    
    private static final VersionRange VERSION_RANGE = new VersionRange(STRING_VERSION_RANGE);

    private static final String BSN = "bsn";
    
    private static final String PROVIDER_BSN = "provider-bsn";
    
    private static final String CONSUMER_BSN = "consumer-bsn";

    private static final String PN = "p";

    private StubBundleDescription bundleDescription;

    private StubImportPackageSpecification importPackage;

    private QuasiBundle qb;

    private StubExportPackageDescription exportPackage;

	private Region stubRegion;

    @Before
    public void setUp() throws BundleException {
        this.bundleDescription = new StubBundleDescription();
        this.bundleDescription.setBundleSymbolicName(BSN);
        this.stubRegion = new StubRegionDigraph().createRegion("testRegion");
        this.qb = new StandardQuasiBundle(this.bundleDescription, null, this.stubRegion);
        this.importPackage = new StubImportPackageSpecification(PN);
        this.exportPackage = new StubExportPackageDescription(PN);
    }

    @Test
    public void testPackageName() {
        QuasiImportPackage qip = new StandardQuasiImportPackage(this.importPackage, this.qb);
        Assert.assertEquals(PN, qip.getPackageName());
    }

    @Test
    public void testVersionConstraint() {
        org.eclipse.osgi.service.resolver.VersionRange versionRange = RESOLVER_VERSION_RANGE;
        this.importPackage.setVersionRange(versionRange);
        QuasiImportPackage qip = new StandardQuasiImportPackage(this.importPackage, qb);
        Assert.assertEquals(VERSION_RANGE, qip.getVersionConstraint());
    }
    
    @Test
    public void testResolved() {
        this.importPackage.setResolved(true);
        QuasiImportPackage qip = new StandardQuasiImportPackage(this.importPackage, this.qb);
        Assert.assertTrue(qip.isResolved());
    }
    
    @Test
    public void testNotResolved() {
        QuasiImportPackage qip = new StandardQuasiImportPackage(this.importPackage, this.qb);
        Assert.assertFalse(qip.isResolved());
    }
    
    @Test
    public void testResolvedProvider() {
        StubBaseDescription supplier = new StubBaseDescription();
        StubBundleDescription bundleSupplier = new StubBundleDescription();
        bundleSupplier.setBundleSymbolicName(PROVIDER_BSN);
        supplier.setSupplier(bundleSupplier);
        this.importPackage.setSupplier(supplier);
        this.importPackage.setResolved(true);
        StubBundleDescription bundleConsumer = new StubBundleDescription();
        bundleConsumer.setBundleSymbolicName(CONSUMER_BSN);
        this.importPackage.setBundle(bundleConsumer);
        this.importPackage.setSupplier(this.exportPackage);
        QuasiImportPackage qip = new StandardQuasiImportPackage(this.importPackage, qb);
        this.exportPackage.setExporter(bundleSupplier);
        Assert.assertEquals(PN, qip.getProvider().getPackageName());
    }
    
    @Test
    public void testUnresolvedProvider() {
        QuasiImportPackage qip = new StandardQuasiImportPackage(this.importPackage, qb);
        Assert.assertNull(qip.getProvider());
    }
    
    @Test
    public void testImportingBundle() {
        QuasiImportPackage qip = new StandardQuasiImportPackage(this.importPackage, qb);
        Assert.assertEquals(qb, qip.getImportingBundle());
    }
    

}
