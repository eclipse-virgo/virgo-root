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

package org.eclipse.virgo.kernel.model.internal.bundle;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.BundleArtifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

public class BundleDependencyDeterminerTests {

    private static final String BUNDLE_TYPE = "bundle";

    private static final Version TEST_BUNDLE_VERSION = Version.emptyVersion;

    private static final String TEST_BUNDLE_NAME = "bundle";

    private static final String EXPORTING_BUNDLE_NAME = "exportingBundle";

    private static final String HOST_BUNDLE_NAME = "hostBundle";

    private static final String REQUIRED_BUNDLE_NAME = "requiredBundle";

    private static final String REGION_A_NAME = "regionA-name";

    private static final String REGION_B_NAME = "regionB-name";

    private static final long BUNDLE_1_ID = 10l;

    private static final long BUNDLE_2_ID = 100l;

    private static final long EXPORTING_BUNDLE_ID = 2l;

    private static final long HOST_BUNDLE_ID = 3l;

    private static final long REQUIRED_BUNDLE_ID = 4l;

    private final QuasiFrameworkFactory quasiFrameworkFactory = createMock(QuasiFrameworkFactory.class);

    private final RuntimeArtifactRepository artifactRepository = new StubArtifactRepository();

    private final RegionDigraph regionDigraph = createMock(RegionDigraph.class);

    private final Region regionA = createMock(Region.class);

    private final Region regionB = createMock(Region.class);

    private final BundleDependencyDeterminer determiner = new BundleDependencyDeterminer(quasiFrameworkFactory, artifactRepository, regionDigraph);

    private QuasiBundle bundle1;

    private QuasiBundle bundle2;

    @Before
    public void setUp() {
        reset(quasiFrameworkFactory, regionDigraph);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullFactory() {
        new BundleDependencyDeterminer(null, artifactRepository, regionDigraph);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRepository() {
        new BundleDependencyDeterminer(quasiFrameworkFactory, null, regionDigraph);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRegionDigraph() {
        new BundleDependencyDeterminer(quasiFrameworkFactory, artifactRepository, null);
    }

    @Test
    public void unknownBundle() {
        QuasiFramework framework = createMock(QuasiFramework.class);
        replay(quasiFrameworkFactory, regionDigraph, framework);

        Set<Artifact> dependents = this.determiner.getDependents(new StubCompositeArtifact("bar", "foo", regionB));
        assertEquals(Collections.<Artifact> emptySet(), dependents);

        verify(quasiFrameworkFactory, regionDigraph, framework);
    }

    @Test
    public void bundleFromDifferentRegion() {
        setTestBundleExpectations();
        QuasiFramework framework = createMock(QuasiFramework.class);
        expect(quasiFrameworkFactory.create()).andReturn(framework);
        expect(framework.getBundles()).andReturn(getTestBundleSet());

        expect(bundle2.getHosts()).andReturn(null).anyTimes();
        expect(bundle2.getRequiredBundles()).andReturn(null).anyTimes();

        expect(regionDigraph.getRegion(BUNDLE_1_ID)).andReturn(regionA);
        expect(regionDigraph.getRegion(BUNDLE_2_ID)).andReturn(regionB);
        expect(regionB.getName()).andReturn(REGION_B_NAME).anyTimes();

        expect(bundle2.getImportPackages()).andReturn(new ArrayList<QuasiImportPackage>());

        BundleArtifact bundleArtifact = createMockBundleArtifact(TEST_BUNDLE_NAME, TEST_BUNDLE_VERSION, regionB);

        replay(bundleArtifact, quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2, regionB);

        Set<Artifact> dependents = this.determiner.getDependents(bundleArtifact);
        assertEquals(Collections.<Artifact> emptySet(), dependents);

        verify(bundleArtifact, quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2, regionB);
    }

    private void setTestBundleExpectations() {
        this.bundle1 = createMockQuasiBundle(TEST_BUNDLE_NAME, TEST_BUNDLE_VERSION, BUNDLE_1_ID);
        this.bundle2 = createMockQuasiBundle(TEST_BUNDLE_NAME, TEST_BUNDLE_VERSION, BUNDLE_2_ID);
    }

    private static QuasiBundle createMockQuasiBundle(String bundleSymbolicName, Version bundleVersion, long bundleId) {
        QuasiBundle quasiBundle = createMock(QuasiBundle.class);
        expect(quasiBundle.getSymbolicName()).andReturn(bundleSymbolicName).anyTimes();
        expect(quasiBundle.getVersion()).andReturn(bundleVersion).anyTimes();
        expect(quasiBundle.getBundleId()).andReturn(bundleId).anyTimes();
        return quasiBundle;
    }

    private static BundleArtifact createMockBundleArtifact(String bundleSymbolicName, Version bundleVersion, Region region) {
        BundleArtifact bundleArtifact = createMock(BundleArtifact.class);
        expect(bundleArtifact.getType()).andReturn(BUNDLE_TYPE).anyTimes();
        expect(bundleArtifact.getName()).andReturn(bundleSymbolicName).anyTimes();
        expect(bundleArtifact.getVersion()).andReturn(bundleVersion).anyTimes();
        expect(bundleArtifact.getRegion()).andReturn(region).anyTimes();
        return bundleArtifact;
    }

    @Test
    public void bundleWithImport() {
        setTestBundleExpectations();
        QuasiFramework framework = createMock(QuasiFramework.class);
        expect(quasiFrameworkFactory.create()).andReturn(framework);
        expect(framework.getBundles()).andReturn(getTestBundleSet());

        QuasiImportPackage importedPackage = createMock(QuasiImportPackage.class);
        QuasiExportPackage provider = createMock(QuasiExportPackage.class);
        QuasiBundle exporter = createMockQuasiBundle(EXPORTING_BUNDLE_NAME, TEST_BUNDLE_VERSION, EXPORTING_BUNDLE_ID);
        expect(provider.getExportingBundle()).andReturn(exporter);
        expect(importedPackage.getProvider()).andReturn(provider);
        ArrayList<QuasiImportPackage> b = new ArrayList<QuasiImportPackage>();
        b.add(importedPackage);
        expect(bundle1.getImportPackages()).andReturn(b);
        expect(bundle1.getHosts()).andReturn(new ArrayList<QuasiBundle>());
        expect(bundle1.getRequiredBundles()).andReturn(new ArrayList<QuasiRequiredBundle>());

        expect(regionDigraph.getRegion(BUNDLE_1_ID)).andReturn(regionA);
        expect(regionDigraph.getRegion(EXPORTING_BUNDLE_ID)).andReturn(regionB);
        expect(regionA.getName()).andReturn(REGION_A_NAME).anyTimes();
        expect(regionB.getName()).andReturn(REGION_B_NAME).anyTimes();

        BundleArtifact bundleArtifact = createMockBundleArtifact(TEST_BUNDLE_NAME, TEST_BUNDLE_VERSION, regionA);
        BundleArtifact exporterArtifact = createMockBundleArtifact(EXPORTING_BUNDLE_NAME, TEST_BUNDLE_VERSION, regionB);

        this.artifactRepository.add(exporterArtifact);

        replay(bundleArtifact, quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2, regionA, regionB, importedPackage, provider,
            exporter, exporterArtifact);

        Set<Artifact> dependents = this.determiner.getDependents(bundleArtifact);
        Set<Artifact> expected = new HashSet<Artifact>();
        expected.add(exporterArtifact);
        assertEquals(expected, dependents);

        verify(bundleArtifact, quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2, regionA, regionB, importedPackage, provider,
            exporter, exporterArtifact);
    }

    @Test
    public void bundleWithHost() {
        setTestBundleExpectations();
        QuasiFramework framework = createMock(QuasiFramework.class);
        expect(quasiFrameworkFactory.create()).andReturn(framework);
        expect(framework.getBundles()).andReturn(getTestBundleSet());

        expect(bundle1.getImportPackages()).andReturn(new ArrayList<QuasiImportPackage>());
        ArrayList<QuasiBundle> b = new ArrayList<QuasiBundle>();
        QuasiBundle host = createMockQuasiBundle(HOST_BUNDLE_NAME, TEST_BUNDLE_VERSION, HOST_BUNDLE_ID);
        b.add(host);
        expect(bundle1.getHosts()).andReturn(b);
        expect(bundle1.getRequiredBundles()).andReturn(new ArrayList<QuasiRequiredBundle>());

        expect(regionDigraph.getRegion(BUNDLE_1_ID)).andReturn(regionA);
        expect(regionDigraph.getRegion(HOST_BUNDLE_ID)).andReturn(regionB);
        expect(regionA.getName()).andReturn(REGION_A_NAME).anyTimes();
        expect(regionB.getName()).andReturn(REGION_B_NAME).anyTimes();

        BundleArtifact bundleArtifact = createMockBundleArtifact(TEST_BUNDLE_NAME, TEST_BUNDLE_VERSION, regionA);
        BundleArtifact hostArtifact = createMockBundleArtifact(HOST_BUNDLE_NAME, TEST_BUNDLE_VERSION, regionB);

        this.artifactRepository.add(hostArtifact);

        replay(bundleArtifact, quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2, regionA, regionB, host, hostArtifact);

        Set<Artifact> dependents = this.determiner.getDependents(bundleArtifact);
        Set<Artifact> expected = new HashSet<Artifact>();
        expected.add(hostArtifact);
        assertEquals(expected, dependents);

        verify(bundleArtifact, quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2, regionA, regionB, host, hostArtifact);
    }

    @Test
    public void bundleRequiringAnotherBundle() {
        setTestBundleExpectations();
        QuasiFramework framework = createMock(QuasiFramework.class);
        expect(quasiFrameworkFactory.create()).andReturn(framework);
        expect(framework.getBundles()).andReturn(getTestBundleSet());

        expect(bundle1.getImportPackages()).andReturn(new ArrayList<QuasiImportPackage>());
        ArrayList<QuasiRequiredBundle> b = new ArrayList<QuasiRequiredBundle>();
        QuasiBundle required = createMockQuasiBundle(REQUIRED_BUNDLE_NAME, TEST_BUNDLE_VERSION, REQUIRED_BUNDLE_ID);
        expect(bundle1.getHosts()).andReturn(new ArrayList<QuasiBundle>());
        QuasiRequiredBundle requiredBundle = createMock(QuasiRequiredBundle.class);
        expect(requiredBundle.getProvider()).andReturn(required);
        b.add(requiredBundle);
        expect(bundle1.getRequiredBundles()).andReturn(b);

        expect(regionDigraph.getRegion(BUNDLE_1_ID)).andReturn(regionA);
        expect(regionDigraph.getRegion(REQUIRED_BUNDLE_ID)).andReturn(regionB);
        expect(regionA.getName()).andReturn(REGION_A_NAME).anyTimes();
        expect(regionB.getName()).andReturn(REGION_B_NAME).anyTimes();

        BundleArtifact bundleArtifact = createMockBundleArtifact(TEST_BUNDLE_NAME, TEST_BUNDLE_VERSION, regionA);
        BundleArtifact requiredArtifact = createMockBundleArtifact(REQUIRED_BUNDLE_NAME, TEST_BUNDLE_VERSION, regionB);

        this.artifactRepository.add(requiredArtifact);

        replay(bundleArtifact, quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2, regionA, regionB, requiredBundle, required,
            requiredArtifact);

        Set<Artifact> dependents = this.determiner.getDependents(bundleArtifact);
        Set<Artifact> expected = new HashSet<Artifact>();
        expected.add(requiredArtifact);
        assertEquals(expected, dependents);

        verify(bundleArtifact, quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2, regionA, regionB, requiredBundle, required,
            requiredArtifact);
    }

    private List<QuasiBundle> getTestBundleSet() {
        List<QuasiBundle> bundles = new ArrayList<QuasiBundle>();
        bundles.add(bundle1);
        bundles.add(bundle2);
        return bundles;
    }

}