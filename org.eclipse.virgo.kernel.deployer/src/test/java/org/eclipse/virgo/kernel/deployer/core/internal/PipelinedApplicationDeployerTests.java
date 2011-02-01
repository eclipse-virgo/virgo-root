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
package org.eclipse.virgo.kernel.deployer.core.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.virgo.kernel.deployer.core.DeployUriNormaliser;
import org.eclipse.virgo.kernel.deployer.core.DeployerConfiguration;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.event.DeploymentListener;
import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeInclosure;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironmentFactory;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class PipelinedApplicationDeployerTests {

    private PipelinedApplicationDeployer pipelinedApplicationDeployer;
    private Pipeline pipeline;
    private InstallArtifactTreeInclosure installArtifactTreeInclosure;
    private InstallEnvironmentFactory installEnvironmentFactory;
    private RuntimeArtifactModel ram;
    private DeploymentListener deploymentListener;
    private EventLogger eventLogger;
    private DeployUriNormaliser normaliser;
    private DeployerConfiguration deployerConfiguration;
    private InstallArtifact installArtifact;
    
    @Before
    public void setup() {
        pipeline = createMock(Pipeline.class);
        installArtifactTreeInclosure = createMock(InstallArtifactTreeInclosure.class);
        installEnvironmentFactory = createMock(InstallEnvironmentFactory.class);
        ram = createMock(RuntimeArtifactModel.class);
        deploymentListener = createMock(DeploymentListener.class);
        eventLogger = createNiceMock(EventLogger.class);
        normaliser = createMock(DeployUriNormaliser.class);
        deployerConfiguration = createMock(DeployerConfiguration.class);
        installArtifact = createMock(InstallArtifact.class);
        
        expect(this.deployerConfiguration.getDeploymentTimeoutSeconds()).andReturn(5);
    }

    @After
    public void tearDown() {
        resetMocks();
    }

    private void startTests() {
        replay(this.pipeline, this.installArtifactTreeInclosure, this.installEnvironmentFactory, this.ram, this.deploymentListener, this.eventLogger, this.normaliser, this.deployerConfiguration, this.installArtifact);
        this.pipelinedApplicationDeployer = new PipelinedApplicationDeployer(pipeline, installArtifactTreeInclosure, installEnvironmentFactory, ram, deploymentListener, eventLogger, normaliser, deployerConfiguration);
    }

    private void verifyMocks() {
        verify(this.pipeline, this.installArtifactTreeInclosure, this.installEnvironmentFactory, this.ram, this.deploymentListener, this.eventLogger, this.normaliser, this.deployerConfiguration, this.installArtifact);
    }

    private void resetMocks() {
        reset(this.pipeline, this.installArtifactTreeInclosure, this.installEnvironmentFactory, this.ram, this.deploymentListener, this.eventLogger, this.normaliser, this.deployerConfiguration, this.installArtifact);
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
    public void testIsdeployedSucsess() throws URISyntaxException, DeploymentException {
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
