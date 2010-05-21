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

package org.eclipse.virgo.apps.admin.core.artifact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.easymock.EasyMock;
import org.junit.Test;

import org.eclipse.virgo.apps.admin.core.artifact.StandardArtifactService;
import org.eclipse.virgo.apps.admin.core.stubs.StubWorkArea;
import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;

/**
 */
public class StandardArtifactServiceTests {
        
    private final ApplicationDeployer deployer = EasyMock.createNiceMock(ApplicationDeployer.class);
    
    private final StandardArtifactService standardArtifactService = new StandardArtifactService(this.deployer, new StubWorkArea());

    @Test
    public void testDeploy() {
        String deploy = this.standardArtifactService.deploy(new File("IExist"));
        assertEquals("Artifact deployed", deploy);
    }

    @Test
    public void testDeployError() throws DeploymentException {
        File stagedFile = new File("IDontExist");
        
        EasyMock.expect(this.deployer.deploy(stagedFile.toURI())).andThrow(new DeploymentException("it went wrong"));
        EasyMock.replay(this.deployer);
        
		String deploy = this.standardArtifactService.deploy(stagedFile);
        assertEquals("Deployment Error 'it went wrong'", deploy);
    }
    
    @Test
    public void testGetStagingDirectory() {
        assertTrue(this.standardArtifactService.getStagingDirectory().getPath().contains("target"));
    }

}
