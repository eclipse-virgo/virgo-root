/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution (StandardInstalArtifactTreeInclosureTests.java)
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.fs.StandardArtifactFSFactory;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.nano.core.BundleStarter;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphFactory;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphInclosure;
import org.eclipse.virgo.kernel.install.artifact.internal.bundle.BundleInstallArtifactGraphFactory;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironmentFactory;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

public class StandardInstallArtifactGraphInclosureTests {

    private static final String PROVISIONING_PROPERTY_NAME = "org.eclipse.virgo.kernel.provisioning";

    private static final String TEST_BUNDLE_REPOSITORY_NAME = "testBundleRepositoryName";

    private final InstallEnvironmentFactory installEnvironmentFactory = createMock(InstallEnvironmentFactory.class);

    private final Pipeline refreshPipeline = createMock(Pipeline.class);

    private InstallArtifactGraphInclosure installArtifactFactory;

    private URI bundleURI;

    private OsgiFramework osgiFramework;

    private BundleStarter bundleStarter;

    private TracingService tracingService;

    private PackageAdminUtil packageAdminUtil;

    private Repository repository;

    private RepositoryAwareArtifactDescriptor artifactDescriptor;

    private final ArtifactStorageFactory artifactStorageFactory = new StandardArtifactStorageFactory(
        new PathReference("build/work/deployer.staging"), new StandardArtifactFSFactory(), new MockEventLogger(), "true");

    private Set<ArtifactBridge> testArtifactBridges = new HashSet<ArtifactBridge>();

    private ThreadSafeDirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();

    @Before
    public void setUp() {
        this.bundleURI = new File("src/test/resources/artifacts/simple.jar").toURI();
        this.osgiFramework = createMock(OsgiFramework.class);
        this.bundleStarter = createMock(BundleStarter.class);
        this.tracingService = createMock(TracingService.class);
        this.packageAdminUtil = createMock(PackageAdminUtil.class);
        this.repository = createMock(Repository.class);
        this.artifactDescriptor = createMock(RepositoryAwareArtifactDescriptor.class);
        this.testArtifactBridges.add(new StubArtifactBridge("bundle", ".jar"));
    }

    private void replayMocks() {
        replay(this.osgiFramework, this.bundleStarter, this.tracingService, this.packageAdminUtil, this.repository, this.artifactDescriptor);
    }

    private void verifyMocks() {
        verify(this.osgiFramework, this.bundleStarter, this.tracingService, this.packageAdminUtil, this.repository, this.artifactDescriptor);
    }

    private void resetMocks() {
        reset(this.osgiFramework, this.bundleStarter, this.tracingService, this.packageAdminUtil, this.repository, this.artifactDescriptor);
    }

    @Test
    public void testBundle() throws DeploymentException, IOException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundleContext userRegionBundleContext = new StubBundleContext();
        expect(this.osgiFramework.getBundleContext()).andReturn(bundleContext).anyTimes();

        replayMocks();

        StandardInstallArtifactRefreshHandler refreshHelper = new StandardInstallArtifactRefreshHandler(installEnvironmentFactory, refreshPipeline);

        bundleContext.registerService(InstallArtifactGraphFactory.class.getName(), new BundleInstallArtifactGraphFactory(this.osgiFramework,
            bundleContext, refreshHelper, this.bundleStarter, this.tracingService, this.packageAdminUtil, userRegionBundleContext,
            new MockEventLogger(), null, dag), null);

        this.installArtifactFactory = new StandardInstallArtifactGraphInclosure(this.artifactStorageFactory, bundleContext, new MockEventLogger());

        Map<String, String> properties = determineDeploymentProperties(Collections.<String, String> emptyMap(), Provisioning.AUTO);

        File artifact = new File(this.bundleURI);
        ArtifactIdentity identity = new ArtifactIdentity("bundle", "a", new Version(1, 2, 3), null);

        GraphNode<InstallArtifact> installGraph = this.installArtifactFactory.constructGraphNode(identity, artifact, properties,
            TEST_BUNDLE_REPOSITORY_NAME);

        InstallArtifact installArtifact = installGraph.getValue();
        assertNotNull(installArtifact);
        assertEquals(TEST_BUNDLE_REPOSITORY_NAME, installArtifact.getRepositoryName());
        assertTrue(installArtifact instanceof BundleInstallArtifact);
        BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
        assertEquals("a", bundleInstallArtifact.getBundleManifest().getBundleSymbolicName().getSymbolicName());

        verifyMocks();
        resetMocks();
    }

    private Map<String, String> determineDeploymentProperties(Map<String, String> properties, Provisioning parentProvisioning) {
        Map<String, String> deploymentProperties = new HashMap<String, String>(properties);
        deploymentProperties.put(PROVISIONING_PROPERTY_NAME, parentProvisioning.toString());
        return deploymentProperties;
    }

    @Test
    public void testBundleImplicitTypeAndVersion() throws DeploymentException, IOException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundleContext userRegionBundleContext = new StubBundleContext();
        expect(this.osgiFramework.getBundleContext()).andReturn(bundleContext).anyTimes();

        replayMocks();

        StandardArtifactIdentityDeterminer artifactIdentityDeterminer = new StandardArtifactIdentityDeterminer(testArtifactBridges);

        StandardInstallArtifactRefreshHandler refreshHelper = new StandardInstallArtifactRefreshHandler(installEnvironmentFactory, refreshPipeline);

        bundleContext.registerService(InstallArtifactGraphFactory.class.getName(), new BundleInstallArtifactGraphFactory(this.osgiFramework,
            bundleContext, refreshHelper, this.bundleStarter, this.tracingService, this.packageAdminUtil, userRegionBundleContext,
            new MockEventLogger(), null, dag), null);

        this.installArtifactFactory = new StandardInstallArtifactGraphInclosure(this.artifactStorageFactory, bundleContext, new MockEventLogger());

        File bundle = new File(this.bundleURI);
        GraphNode<InstallArtifact> installArtifactGraph = this.installArtifactFactory.constructGraphNode(
            artifactIdentityDeterminer.determineIdentity(bundle, null), bundle, null, null);
        checkBundleImplicitTypeAndVersion(installArtifactGraph.getValue());

        verifyMocks();
        resetMocks();
    }

    @Test
    public void testNoBSNBundleImplicitTypeAndVersion() throws DeploymentException, URISyntaxException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundleContext userRegionBundleContext = new StubBundleContext();
        expect(this.osgiFramework.getBundleContext()).andReturn(bundleContext).anyTimes();

        replayMocks();

        StandardArtifactIdentityDeterminer artifactIdentityDeterminer = new StandardArtifactIdentityDeterminer(testArtifactBridges);

        StandardInstallArtifactRefreshHandler refreshHelper = new StandardInstallArtifactRefreshHandler(installEnvironmentFactory, refreshPipeline);

        bundleContext.registerService(InstallArtifactGraphFactory.class.getName(), new BundleInstallArtifactGraphFactory(this.osgiFramework,
            bundleContext, refreshHelper, this.bundleStarter, this.tracingService, this.packageAdminUtil, userRegionBundleContext,
            new MockEventLogger(), null, dag), null);

        this.installArtifactFactory = new StandardInstallArtifactGraphInclosure(this.artifactStorageFactory, bundleContext, new MockEventLogger());

        File bundle = new File("src/test/resources/artifacts/nobsn.jar");
        GraphNode<InstallArtifact> installArtifactGraph = this.installArtifactFactory.constructGraphNode(
            artifactIdentityDeterminer.determineIdentity(bundle, null), bundle, null, null);
        InstallArtifact installArtifact = installArtifactGraph.getValue();
        assertNotNull(installArtifact);
        assertTrue(installArtifact instanceof BundleInstallArtifact);
        assertEquals("nobsn", installArtifact.getName());
        assertEquals(new Version("0"), installArtifact.getVersion());

        verifyMocks();
        resetMocks();
    }

    @Test
    public void testRecoverBundleImplicitTypeAndVersion() throws DeploymentException, IOException {
        StubBundleContext bundleContext = new StubBundleContext();
        StubBundleContext userRegionBundleContext = new StubBundleContext();
        expect(this.osgiFramework.getBundleContext()).andReturn(bundleContext).anyTimes();

        replayMocks();

        StandardInstallArtifactRefreshHandler refreshHelper = new StandardInstallArtifactRefreshHandler(installEnvironmentFactory, refreshPipeline);

        StandardArtifactIdentityDeterminer artifactIdentityDeterminer = new StandardArtifactIdentityDeterminer(testArtifactBridges);

        bundleContext.registerService(InstallArtifactGraphFactory.class.getName(), new BundleInstallArtifactGraphFactory(this.osgiFramework,
            bundleContext, refreshHelper, this.bundleStarter, this.tracingService, this.packageAdminUtil, userRegionBundleContext,
            new MockEventLogger(), null, dag), null);

        this.installArtifactFactory = new StandardInstallArtifactGraphInclosure(this.artifactStorageFactory, bundleContext, new MockEventLogger());

        File bundle = new File(this.bundleURI);
        GraphNode<InstallArtifact> installArtifactGraph = this.installArtifactFactory.constructGraphNode(
            artifactIdentityDeterminer.determineIdentity(bundle, null), bundle, null, null);

        checkBundleImplicitTypeAndVersion(installArtifactGraph.getValue());
        
        GraphNode<InstallArtifact> recoveredInstallGraph = this.installArtifactFactory.recoverInstallGraph(artifactIdentityDeterminer.determineIdentity(bundle, null), new File(this.bundleURI));
        checkBundleImplicitTypeAndVersion(recoveredInstallGraph.getValue());

        verifyMocks();
        resetMocks();
    }

    private void checkBundleImplicitTypeAndVersion(InstallArtifact installArtifact) throws IOException {
        assertNotNull(installArtifact);
        assertTrue(installArtifact instanceof BundleInstallArtifact);
        BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
        assertEquals("simple", bundleInstallArtifact.getBundleManifest().getBundleSymbolicName().getSymbolicName());
        assertEquals(new Version("0"), bundleInstallArtifact.getBundleManifest().getBundleVersion());
    }
}
