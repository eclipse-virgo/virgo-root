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

import java.util.List;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.test.stubs.region.StubRegionDigraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 */
public class StandardQuasiBundleTests {

    private static final String BSN = "bsn";
    
    private static final Version BV = new Version("4.3.2.ga");
    
    private static final long BID = 2341341;

    private StubBundleDescription bundleDescription;

	private Region stubRegion;

    @Before
    public void setUp() throws BundleException {
        this.stubRegion = new StubRegionDigraph().createRegion("testRegion");
        this.bundleDescription = new StubBundleDescription();
    }

    @Test
    public void testSymbolicName() {
        bundleDescription.setBundleSymbolicName(BSN);
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        Assert.assertEquals(BSN, qb.getSymbolicName());
    }
    
    @Test
    public void testVersion() {
        bundleDescription.setVersion(BV);
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        Assert.assertEquals(BV, qb.getVersion());
    }
    
    @Test
    public void testIsResolved() {
        bundleDescription.setResolved(true);
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        Assert.assertEquals(true, qb.isResolved());
    }
    
    @Test
    public void testBundleId() {
        bundleDescription.setBundleId(BID);
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        Assert.assertEquals(BID, qb.getBundleId());
    }
    
    @Test
    public void testFragments() {
        bundleDescription.addFragment(new StubBundleDescription("f1"));
        bundleDescription.addFragment(new StubBundleDescription("f2"));
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, this.stubRegion);
        List<QuasiBundle> fragments = qb.getFragments();
        Assert.assertEquals(2, fragments.size());
        Assert.assertEquals("f1", fragments.get(0).getSymbolicName());
        Assert.assertEquals("f2", fragments.get(1).getSymbolicName());
    }
    
    @Test
    public void testNoFragments() {
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        List<QuasiBundle> fragments = qb.getFragments();
        Assert.assertEquals(0, fragments.size());
    }
    
    @Test
    public void testHosts() {
        bundleDescription.addHost(new StubBundleDescription("h1"));
        bundleDescription.addHost(new StubBundleDescription("h2"));
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, this.stubRegion);
        List<QuasiBundle> hosts = qb.getHosts();
        Assert.assertEquals(2, hosts.size());
        Assert.assertEquals("h1", hosts.get(0).getSymbolicName());
        Assert.assertEquals("h2", hosts.get(1).getSymbolicName());
    }
    
    @Test
    public void testNoHosts() {
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        List<QuasiBundle> hosts = qb.getHosts();
        Assert.assertNull(hosts);
    }
    
    @Test
    public void testExportPackages() {
        bundleDescription.addExportPackage(new StubExportPackageDescription("e1"));
        bundleDescription.addExportPackage(new StubExportPackageDescription("e2"));
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        List<QuasiExportPackage> exportPackages = qb.getExportPackages();
        Assert.assertEquals(2, exportPackages.size());
        Assert.assertEquals("e1", exportPackages.get(0).getPackageName());
        Assert.assertEquals("e2", exportPackages.get(1).getPackageName());
    }
    
    @Test
    public void testImportPackages() {
        bundleDescription.addImportPackage(new StubImportPackageSpecification("i1"));
        bundleDescription.addImportPackage(new StubImportPackageSpecification("i2"));
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        List<QuasiImportPackage> importPackages = qb.getImportPackages();
        Assert.assertEquals(2, importPackages.size());
        Assert.assertEquals("i1", importPackages.get(0).getPackageName());
        Assert.assertEquals("i2", importPackages.get(1).getPackageName());
    }
    
    @Test
    public void testRequiredBundles() {
        bundleDescription.addRequiredBundle(new StubBundleSpecification("b1"));
        bundleDescription.addRequiredBundle(new StubBundleSpecification("b2"));
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, null);
        List<QuasiRequiredBundle> requiredBundles = qb.getRequiredBundles();
        Assert.assertEquals(2, requiredBundles.size());
        Assert.assertEquals("b1", requiredBundles.get(0).getRequiredBundleName());
        Assert.assertEquals("b2", requiredBundles.get(1).getRequiredBundleName());
    }
    
    @Test
    public void testDependents() {
        bundleDescription.addDependent(new StubBundleDescription("b1"));
        bundleDescription.addDependent(new StubBundleDescription("b2"));
        QuasiBundle qb = new StandardQuasiBundle(bundleDescription, null, this.stubRegion);
        List<QuasiBundle> requiredBundles = qb.getDependents();
        Assert.assertEquals(2, requiredBundles.size());
        Assert.assertEquals("b1", requiredBundles.get(0).getSymbolicName());
        Assert.assertEquals("b2", requiredBundles.get(1).getSymbolicName());
    }

}
