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

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Test;

public class ActivatorTcclTests extends AbstractDeployerIntegrationTest {
    
    @Test
    public void tcclDuringBundleActivation() throws DeploymentException {
        // Bundle will fail to start if the TCCL is not the bundle's class loader during activator start()
        DeploymentIdentity deployed = this.deployer.deploy(new File("src/test/resources/activator-tccl.jar").toURI());
        // Bundle will fail to stop if the TCCL is not the bundle's class loader during activator stop()
        this.deployer.undeploy(deployed);
    }
}
