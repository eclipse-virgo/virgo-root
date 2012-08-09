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

import java.net.URI;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Test;


public class RepositoryDeploymentTests extends AbstractDeployerIntegrationTest {
    
    @Test
    public void repositoryDeployment() throws Exception {
        DeploymentIdentity deployed = this.deployer.deploy(URI.create("repository:configuration/com.foo.bar/0"));
        this.deployer.undeploy(deployed);
    }
    
    @Test(expected=DeploymentException.class)
    public void repositoryDeploymentOfNonExistentArtifact() throws Exception {
        this.deployer.deploy(URI.create("repository:configuration/com.foo.bar/1"));        
    }
}
