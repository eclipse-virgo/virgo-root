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

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;

/**
 * Test the interactions between the Runtime Artifact Model (RAM) and the deployer.
 * 
 */
public class RAMDeploymentIntegrationTests extends AbstractDeployerIntegrationTest {

    private static final String REGION_USER = "org.eclipse.virgo.region.user";
    
    private StubInstallArtifactLifecycleListener lifecycleListener;

    private ServiceRegistration<InstallArtifactLifecycleListener> lifecycleListenerRegistration;

    private DeploymentIdentity deploymentIdentity;

    private ServiceReference<RuntimeArtifactRepository> ramReference;
    
    private RuntimeArtifactRepository ram;

    private RegionDigraph regionDigraph;

    @Before
    public void setUp() {
        this.lifecycleListener = new StubInstallArtifactLifecycleListener();
        this.lifecycleListenerRegistration = this.kernelContext.registerService(InstallArtifactLifecycleListener.class, this.lifecycleListener, null);

        this.ramReference = this.kernelContext.getServiceReference(RuntimeArtifactRepository.class);
        this.ram = this.kernelContext.getService(ramReference);

        ServiceReference<RegionDigraph> regionDigraphReference = this.kernelContext.getServiceReference(RegionDigraph.class);
        this.regionDigraph = this.kernelContext.getService(regionDigraphReference);
    }

    @After
    public void tearDown() {
        if (this.lifecycleListenerRegistration != null) {
            this.lifecycleListenerRegistration.unregister();
        }
        if (this.ramReference != null) {
            this.ram = null;
            this.kernelContext.ungetService(this.ramReference);
            this.ramReference = null;
        }
    }

    @Test
    public void testRAMUndeployment() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        Artifact artifact = this.ram.getArtifact(this.deploymentIdentity.getType(), this.deploymentIdentity.getSymbolicName(), new Version(this.deploymentIdentity.getVersion()), this.regionDigraph.getRegion(REGION_USER));
        assertNotNull(artifact);
        artifact.uninstall();
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
    }

    @Test(expected = DeploymentException.class)
    public void testRAMUndeploymentFollowedByDeployerUndeployment() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        Artifact artifact = this.ram.getArtifact(this.deploymentIdentity.getType(), this.deploymentIdentity.getSymbolicName(), new Version(this.deploymentIdentity.getVersion()), this.regionDigraph.getRegion(REGION_USER));
        artifact.uninstall();

        // The following deployer operation should throw DeploymentException.
        this.deployer.undeploy(this.deploymentIdentity);
    }

}
