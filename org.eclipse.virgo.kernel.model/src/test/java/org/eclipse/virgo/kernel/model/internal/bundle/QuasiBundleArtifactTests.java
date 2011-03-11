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

package org.eclipse.virgo.kernel.model.internal.bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class QuasiBundleArtifactTests {

    private static final Version TEST_BUNDLE_VERSION = new Version("4345.234.56.sfghz");

    private static final String TEST_BUNDLE_SYMBOLIC_NAME = "test.bundle";

    private QuasiBundle mockQuasiBundle;

    private Bundle mockBundle;

    private PackageAdminUtil mockPackageAdminUtil;

    private Artifact quasiBundleArtifact;

    private BundleContext mockBundleContext;

    private Filter mockFilter;

    @Before
    public void setUp() throws Exception {
        this.mockQuasiBundle = EasyMock.createMock(QuasiBundle.class);
        EasyMock.expect(this.mockQuasiBundle.getSymbolicName()).andReturn(TEST_BUNDLE_SYMBOLIC_NAME).anyTimes();
        EasyMock.expect(this.mockQuasiBundle.getVersion()).andReturn(TEST_BUNDLE_VERSION).anyTimes();
        
        this.mockBundle = EasyMock.createMock(Bundle.class);
        this.mockBundleContext = EasyMock.createMock(BundleContext.class);
        this.mockFilter = EasyMock.createMock(Filter.class);
        EasyMock.expect(this.mockBundle.getSymbolicName()).andReturn(TEST_BUNDLE_SYMBOLIC_NAME).anyTimes();
        EasyMock.expect(this.mockBundle.getVersion()).andReturn(TEST_BUNDLE_VERSION).anyTimes();
        EasyMock.expect(this.mockBundle.getBundleContext()).andReturn(this.mockBundleContext).anyTimes();
        EasyMock.expect(this.mockBundle.getState()).andReturn(Bundle.ACTIVE).anyTimes();
        EasyMock.expect(this.mockBundleContext.createFilter(EasyMock.isA(String.class))).andReturn(this.mockFilter).anyTimes();
        this.mockBundleContext.addServiceListener(EasyMock.isA(ServiceListener.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();
        ServiceReference<?>[] refs = {};
        EasyMock.expect(this.mockBundleContext.getServiceReferences((String) EasyMock.eq(null), EasyMock.isA(String.class))).andReturn(refs).anyTimes();
        
        EasyMock.expect(this.mockQuasiBundle.getBundle()).andReturn(this.mockBundle).anyTimes();
        
        this.mockPackageAdminUtil = EasyMock.createMock(PackageAdminUtil.class);
        this.quasiBundleArtifact = new QuasiBundleArtifact(this.mockQuasiBundle, this.mockPackageAdminUtil);
    }

    private void replay() {
        EasyMock.replay(this.mockQuasiBundle, this.mockPackageAdminUtil, this.mockBundle, this.mockBundleContext, this.mockFilter);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(this.mockQuasiBundle, this.mockPackageAdminUtil, this.mockBundle, this.mockBundleContext, this.mockFilter);
    }

    @Test
    public void testStart() throws BundleException {
        this.mockBundle.start();
        EasyMock.expectLastCall();
        replay();
        this.quasiBundleArtifact.start();
    }

    @Test
    public void testStop() throws BundleException {
        this.mockBundle.stop();
        EasyMock.expectLastCall();
        replay();
        this.quasiBundleArtifact.stop();
    }

    @Test
    public void testRefresh() throws BundleException {
        this.mockBundle.update();
        Bundle[] bundles = { this.mockBundle };
        this.mockPackageAdminUtil.synchronouslyRefreshPackages(EasyMock.aryEq(bundles));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expectLastCall();
        replay();
        this.quasiBundleArtifact.refresh();
    }

    @Test
    public void testUninstall() throws BundleException {
        this.mockBundle.uninstall();
        EasyMock.expectLastCall();
        replay();
        this.quasiBundleArtifact.uninstall();
    }

    @Test
    public void testGetType() {
        replay();
        Assert.assertEquals("bundle", this.quasiBundleArtifact.getType());
    }

    @Test
    public void testGetName() {
        replay();
        Assert.assertEquals(TEST_BUNDLE_SYMBOLIC_NAME, this.quasiBundleArtifact.getName());
    }

    @Test
    public void testGetVersion() {
        replay();
        Assert.assertEquals(TEST_BUNDLE_VERSION, this.quasiBundleArtifact.getVersion());
    }

    @Test
    public void testGetState() {
        replay();
        Assert.assertEquals(ArtifactState.ACTIVE, this.quasiBundleArtifact.getState());
    }

    @Test
    public void testGetDependents() {
        QuasiBundle qb1 = EasyMock.createMock(QuasiBundle.class);
        QuasiBundle qb2 = EasyMock.createMock(QuasiBundle.class);
        List<QuasiBundle> dependents = new ArrayList<QuasiBundle>();
        dependents.add(qb1);
        dependents.add(qb2);
        EasyMock.expect(this.mockQuasiBundle.getDependents()).andReturn(dependents);
        replay();
        Set<Artifact> expected = new HashSet<Artifact>();
        expected.add(new QuasiBundleArtifact(qb1, this.mockPackageAdminUtil));
        expected.add(new QuasiBundleArtifact(qb2, this.mockPackageAdminUtil));
        Assert.assertEquals(expected, this.quasiBundleArtifact.getDependents());
    }

    @Test
    public void testGetProperties() {
        replay();
        Assert.assertEquals(new HashMap<String, String>(), this.quasiBundleArtifact.getProperties());
    }

}