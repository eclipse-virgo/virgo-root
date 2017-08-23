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
import static org.junit.Assert.assertNull;

import java.io.File;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Test;


/**
 * Test deploying an OSGi application containing a simple bundle.
 * 
 */
public class SimpleBundleApplicationDeploymentTests extends AbstractDeployerIntegrationTest {

    private static final String TEST_BUNDLE_SYMBOLIC_NAME = "MyApp-1-com.springsource.kernel.deployer.testbundle";

    @Test public void testDeployer() throws Exception {
        File file = new File("src/test/resources/app0.par");

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(file.toURI());
        // Check that the test bundle's application context is created.
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_SYMBOLIC_NAME));        

        this.deployer.undeploy(deploymentIdentity);
        // Check that the test bundle's application context is destroyed.
        assertNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_SYMBOLIC_NAME));

        // Check that the application can be deployed again after being undeployed
        this.deployer.deploy(file.toURI());
        // Check that the test bundle's application context is created.
        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_SYMBOLIC_NAME));
    }

}
