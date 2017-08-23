/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.core.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.virgo.nano.deployer.api.core.DeployUriNormaliser;
import org.eclipse.virgo.nano.deployer.api.core.DeployerConfiguration;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.event.DeploymentListener;
import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphInclosure;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironmentFactory;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PipelinedApplicationDeployerTests {

    private PipelinedApplicationDeployer pipelinedApplicationDeployer;

    private Pipeline pipeline;

    private InstallArtifactGraphInclosure installArtifactTreeInclosure;

    private ArtifactIdentityDeterminer artifactIdentityDeterminer;

    private InstallEnvironmentFactory installEnvironmentFactory;

    private RuntimeArtifactModel ram;

    private DeploymentListener deploymentListener;

    private EventLogger eventLogger;

    private DeployUriNormaliser normaliser;

    private DeployerConfiguration deployerConfiguration;

    private InstallArtifact installArtifact;

    private final StubBundleContext stubBundleContext = new StubBundleContext();

    @Before
    public void setup() {
        this.pipeline = createMock(Pipeline.class);
        this.installArtifactTreeInclosure = createMock(InstallArtifactGraphInclosure.class);
        this.artifactIdentityDeterminer = createMock(ArtifactIdentityDeterminer.class);
        this.installEnvironmentFactory = createMock(InstallEnvironmentFactory.class);
        this.ram = createMock(RuntimeArtifactModel.class);
        this.deploymentListener = createMock(DeploymentListener.class);
        this.eventLogger = createNiceMock(EventLogger.class);
        this.normaliser = createMock(DeployUriNormaliser.class);
        this.deployerConfiguration = createMock(DeployerConfiguration.class);
        this.installArtifact = createMock(InstallArtifact.class);
        expect(this.deployerConfiguration.getDeploymentTimeoutSeconds()).andReturn(5);
    }

    @After
    public void tearDown() {
        resetMocks();
    }

    private void startTests() {
        replay(this.pipeline, this.installArtifactTreeInclosure, this.installEnvironmentFactory, this.ram, this.deploymentListener, this.eventLogger,
            this.normaliser, this.deployerConfiguration, this.installArtifact);
        this.pipelinedApplicationDeployer = new PipelinedApplicationDeployer(this.pipeline, this.installArtifactTreeInclosure,
            this.artifactIdentityDeterminer, this.installEnvironmentFactory, this.ram, this.deploymentListener, this.eventLogger, this.normaliser,
            this.deployerConfiguration, this.stubBundleContext);
    }

    private void verifyMocks() {
        verify(this.pipeline, this.installArtifactTreeInclosure, this.installEnvironmentFactory, this.ram, this.deploymentListener, this.eventLogger,
            this.normaliser, this.deployerConfiguration, this.installArtifact);
    }

    private void resetMocks() {
        reset(this.pipeline, this.installArtifactTreeInclosure, this.installEnvironmentFactory, this.ram, this.deploymentListener, this.eventLogger,
            this.normaliser, this.deployerConfiguration, this.installArtifact);
    }

    @Test
    public void testIsdeployedFalse() throws URISyntaxException, DeploymentException {
        URI testURI = new URI("foo");
        expect(this.normaliser.normalise(testURI)).andReturn(null);
        this.startTests();
        boolean result = this.pipelinedApplicationDeployer.isDeployed(testURI);
        this.verifyMocks();
        assertFalse(result);
    }

    @Test
    public void testIsdeployedFalse2() throws URISyntaxException, DeploymentException {
        URI testURI = new URI("foo");
        expect(this.normaliser.normalise(testURI)).andReturn(testURI);
        expect(this.ram.get(testURI)).andReturn(null);
        this.startTests();
        boolean result = this.pipelinedApplicationDeployer.isDeployed(testURI);
        this.verifyMocks();
        assertFalse(result);
    }

    @Test
    public void testIsdeployedSucsess() throws Exception {
        URI testURI = new URI("foo");
        expect(this.normaliser.normalise(testURI)).andReturn(testURI);
        expect(this.ram.get(testURI)).andReturn(this.installArtifact);
        this.startTests();
        boolean result = this.pipelinedApplicationDeployer.isDeployed(testURI);
        this.verifyMocks();
        assertTrue(result);
    }

    @Test
    public void testIsdeployedFail() throws URISyntaxException, DeploymentException {
        URI testURI = new URI("foo");
        expect(this.normaliser.normalise(testURI)).andThrow(new DeploymentException("fail"));
        this.startTests();
        boolean result = this.pipelinedApplicationDeployer.isDeployed(testURI);
        this.verifyMocks();
        assertFalse(result);
    }

}
