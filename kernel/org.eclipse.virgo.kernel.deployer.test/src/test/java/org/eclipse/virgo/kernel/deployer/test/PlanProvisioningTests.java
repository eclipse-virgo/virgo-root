/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
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

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class PlanProvisioningTests extends AbstractDeployerIntegrationTest {
    
    @Test
    public void testDefaultProvisioning() throws Exception {
        testPlanDeployment("src/test/resources/plan-provisioning/default.plan");
    }

    @Test
    public void testInheritProvisioning() throws Exception {
        testPlanDeployment("src/test/resources/plan-provisioning/inherit.plan");
    }

    @Test
    public void testAutoProvisioning() throws Exception {
        testPlanDeployment("src/test/resources/plan-provisioning/auto.plan");
    }

    @Test(expected = DeploymentException.class)
    public void testDisabledProvisioning() throws Exception {
        testPlanDeployment("src/test/resources/plan-provisioning/disabled.plan");
    }
    
    @Test
    public void testAutoInheritProvisioning() throws Exception {
        testPlanDeployment("src/test/resources/plan-provisioning/auto-inherit.plan");
    }

    @Test(expected = DeploymentException.class)
    public void testDisabledInheritProvisioning() throws Exception {
        testPlanDeployment("src/test/resources/plan-provisioning/disabled-inherit.plan");
    }

    @Test(expected = DeploymentException.class)
    public void testDisabledDefaultProvisioning() throws Exception {
        testPlanDeployment("src/test/resources/plan-provisioning/disabled-default.plan");
    }
    
    @Test
    public void testParentWithExporterDisabledProvisioning() throws Exception {
        testPlanDeployment("src/test/resources/plan-provisioning/parent-with-exporter-disabled.plan");
    }
    
    private void testPlanDeployment(String planPath) throws Exception {
        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File(planPath).toURI());
        this.deployer.undeploy(deploymentIdentity);
        Bundle exporter = getBundle("plan.provisioning.p.exporter", Version.emptyVersion);
        if (exporter != null) {
            exporter.uninstall();
        }
    }

 
}
