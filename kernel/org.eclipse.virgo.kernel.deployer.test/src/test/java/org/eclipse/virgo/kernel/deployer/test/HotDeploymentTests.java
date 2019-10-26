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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.management.ManagementFactory;

import javax.management.JMException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;


import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.util.io.PathReference;


public class HotDeploymentTests extends AbstractDeployerIntegrationTest {

	private static final String ORG_ECLIPSE_VIRGO_REGION_USER = "org.eclipse.virgo.region.user";
    
    private static final String GLOBAL_REGION = "global";

	private static final long TIMEOUT = 10000;

    @Test
    public void programmaticUndeployDeletesHotDeployedPropertiesFile() throws DeploymentException {
        PathReference pathReference = new PathReference("src/test/resources/hot-deployment-tests/test.properties");
        doTest(pathReference, "configuration", "0.0.0");
    }
    
    @Test
    public void programmaticUndeployDeletesHotDeployedExplodedBundle() throws DeploymentException {
        PathReference pathReference = new PathReference("src/test/resources/hot-deployment-tests/bundle");
        doTest(pathReference, "bundle", "1.0.0");
    }
    
    @Test
    public void programmaticUndeployDeletesHotDeployedBundle() throws DeploymentException {
        PathReference pathReference = new PathReference("src/test/resources/hot-deployment-tests/bundle.jar");
        doTest(pathReference, "bundle", "1.0.0");
    }
    
    private void doTest(PathReference artifact, String type, String version) throws DeploymentException {
        PathReference copyInPickup = artifact.copy(new PathReference("build/pickup"), true);

        try {
            while (!this.deployer.isDeployed(copyInPickup.toURI())) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }

            awaitActive(type, "test", version);
            
            this.deployer.undeploy(type, "test", version);
            
            assertFalse(copyInPickup.toFile().exists());
            
        } finally {
            copyInPickup.delete();
        }
    }

    private static void awaitActive(String type, String name, String version) {
        String region = ORG_ECLIPSE_VIRGO_REGION_USER;
    	if(type.equals("configuration")){
    		region = GLOBAL_REGION;
    	}
        try {
            ObjectName objectName = new ObjectName("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=" + type + ",name=" + name + ",version=" + version + ",region=" + region);
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ManageableArtifact artifact = JMX.newMXBeanProxy(mBeanServer, objectName, ManageableArtifact.class);
            
            long startTime = System.currentTimeMillis();
            
            while (!"ACTIVE".equals(artifact.getState())) {
                if (System.currentTimeMillis() - startTime > TIMEOUT) {
                    fail("Artifact " + type + " " + name + " " + version + " was not active within " + TIMEOUT + "ms.");
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
        } catch (JMException jme) {
            fail(jme.getMessage());
        }
    }
}
