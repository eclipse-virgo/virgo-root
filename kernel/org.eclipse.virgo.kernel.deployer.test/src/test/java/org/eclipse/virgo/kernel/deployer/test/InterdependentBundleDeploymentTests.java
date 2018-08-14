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

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Test deploying bundles that depend on each other.
 * 
 */
public class InterdependentBundleDeploymentTests extends AbstractDeployerIntegrationTest {

    private ServiceReference<ApplicationDeployer> appDeployerServiceReference;

    private ApplicationDeployer appDeployer;

    @Before public void setUp() {
        PathReference pr = new PathReference("./target/org.eclipse.virgo.kernel");
        pr.delete(true);
        pr.createDirectory();
        
        this.appDeployerServiceReference = this.context.getServiceReference(ApplicationDeployer.class);
        this.appDeployer = this.context.getService(this.appDeployerServiceReference);
    }

    @After public void tearDown() {
        if (this.appDeployerServiceReference != null) {
            this.context.ungetService(this.appDeployerServiceReference);
        }
    }

    @Test public void testImportBundleDependency() throws Exception {
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(new File("src/test/resources/osgi_test.jar").toURI());
        DeploymentIdentity deploymentIdentity2 = this.appDeployer.deploy(new File("src/test/resources/osgi_test2.jar").toURI());
        this.appDeployer.undeploy(deploymentIdentity2);
        this.appDeployer.undeploy(deploymentIdentity);
    }
    
    @Test
    @Ignore("[DMS-2883] Fails intermittently due to problem described in ENGINE-1755")
    public void testUndeploymentOrder() throws Exception {
        DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(new File("src/test/resources/osgi_test.jar").toURI());
        DeploymentIdentity deploymentIdentity2 = this.appDeployer.deploy(new File("src/test/resources/osgi_test2.jar").toURI());
        this.appDeployer.undeploy(deploymentIdentity);
        this.appDeployer.undeploy(deploymentIdentity2);
    }

}
