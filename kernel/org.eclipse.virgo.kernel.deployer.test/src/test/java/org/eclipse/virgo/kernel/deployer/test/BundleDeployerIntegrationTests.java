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
 * Test deploying a bundle devoid of personality.
 * 
 */
public class BundleDeployerIntegrationTests extends AbstractDeployerIntegrationTest {

    private static final String TEST_BUNDLE_TYPE = "bundle";

    private static final String TEST_BUNDLE_SYMBOLIC_NAME = "com.springsource.kernel.deployer.testbundle";

    private static final String TEST_BUNDLE_VERSION = "2.0.0.build-20080229163630";

    @Test
    public void testDeployer() throws Exception {
        File file = new File("src/test/resources/com.springsource.platform.deployer.testbundle.jar");

        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        
        assertDeploymentIdentityEquals(deploymentId, "testbundle.jar", TEST_BUNDLE_TYPE, TEST_BUNDLE_SYMBOLIC_NAME, TEST_BUNDLE_VERSION);

        assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_SYMBOLIC_NAME));

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());

        // Check that the test bundle's application context is destroyed.
        assertNull(ApplicationContextUtils.getApplicationContext(this.context, TEST_BUNDLE_SYMBOLIC_NAME));
    }
}
