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

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Test the generic deployer using a test deployer. This doesn't check OSGi behaviour but will detect clashes in the
 * file system during deployment.
 * 
 */
// TODO Additional config paths in the new test framework
// @AdditionalConfigPaths("src/test/resources/configTimeout/")
// TODO This test is not robust: it passes without the change to the configured timeout value
@Ignore
public class PipelinedDeployerTimeOutTests extends AbstractDeployerIntegrationTest {

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    private final PathReference pickup = new PathReference("./target/pickup");

    private StubInstallArtifactLifecycleListener lifecycleListener;

    private ServiceRegistration<InstallArtifactLifecycleListener> lifecycleListenerRegistration;

    @Before
    public void setUp() {
        PathReference pr = new PathReference("./target/deployer");
        pr.delete(true);
        pr.createDirectory();

        clearPickup();

        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);
        this.lifecycleListener = new StubInstallArtifactLifecycleListener();
        this.lifecycleListenerRegistration = this.context.registerService(InstallArtifactLifecycleListener.class, this.lifecycleListener, null);
    }

    private void clearPickup() {
        File[] contents = this.pickup.toFile().listFiles();
        for (File file : contents) {
            file.delete();
        }
    }

    @After
    public void tearDown() {
        clearPickup();
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
        if (this.lifecycleListenerRegistration != null) {
            this.lifecycleListenerRegistration.unregister();
        }
    }

    @Test
    public void testZeroTimeoutDeployer() throws Exception {
        File file = new File("src/test/resources/ExporterC.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        this.appDeployer.undeploy(deploymentIdentity);
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
    }
}
