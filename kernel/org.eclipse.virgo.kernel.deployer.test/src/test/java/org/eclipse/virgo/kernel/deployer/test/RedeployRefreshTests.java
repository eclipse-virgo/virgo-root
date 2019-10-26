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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.*;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Test deploying a simple bundle, then a bad version, then a good version again.
 * 
 */
public class RedeployRefreshTests extends AbstractDeployerIntegrationTest {

    private static final DeploymentOptions OPTIONS_NON_RECOVERABLE = new DeploymentOptions(false, false, true);

    private static final DeploymentOptions OPTIONS_OWNED = new DeploymentOptions(false, true, true);

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    private PathReference simpleModule;

    @Before
    public void setUp() {
        // The following races with server startup and causes unpleasantness on
        // the console, but is unfortunately
        // necessary until the test framework cold starts the server.
        PathReference pr = new PathReference("./build/redeploy-refresh");
        pr.delete(true);
        pr.createDirectory();
        simpleModule = pr.newChild("simple.module.jar");

        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);
    }

    @After
    public void tearDown() {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
    }

    @Test
    public void testRedeployRefresh() throws Exception {
        PathReference goodModule = new PathReference("src/test/resources/redeploy-refresh/good/simple.module.jar");
        goodModule.copy(this.simpleModule);
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(this.simpleModule.toURI(), OPTIONS_NON_RECOVERABLE);

        PathReference badModule = new PathReference("src/test/resources/redeploy-refresh/bad/simple.module.jar");
        this.simpleModule.delete();
        badModule.copy(this.simpleModule);
        try {
            deploymentIdentity = this.appDeployer.deploy(this.simpleModule.toURI(), OPTIONS_NON_RECOVERABLE);
            Assert.fail();
        } catch (DeploymentException e) {
            // Expected
        }

        this.simpleModule.delete();
        goodModule.copy(this.simpleModule);
        deploymentIdentity = this.appDeployer.deploy(this.simpleModule.toURI(), OPTIONS_NON_RECOVERABLE);

        this.appDeployer.undeploy(deploymentIdentity);
    }

    @Test
    public void testRedeployRefreshOwned() throws Exception {
        PathReference goodModule = new PathReference("src/test/resources/redeploy-refresh/good/simple.module.jar");
        goodModule.copy(this.simpleModule);

        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(this.simpleModule.toURI(), OPTIONS_OWNED);
        String symbolicName = deploymentIdentity.getSymbolicName();

        PathReference good2Module = new PathReference("src/test/resources/redeploy-refresh/good/simple2.module.jar");
        this.simpleModule.delete();
        good2Module.copy(this.simpleModule);

        DeploymentIdentity newDeploymentIdentity = this.appDeployer.refresh(this.simpleModule.toURI(), symbolicName); // should redeploy

        assertEquals("Refreshed application doesn't have correct version", "2.0.0.BUILD-20090219114136", newDeploymentIdentity.getVersion());

        this.appDeployer.undeploy(newDeploymentIdentity.getType(), newDeploymentIdentity.getSymbolicName(), newDeploymentIdentity.getVersion());

        assertFalse("File should not exist", this.simpleModule.exists());
    }

}
