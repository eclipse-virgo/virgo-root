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

import java.io.File;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.junit.Test;

/**
 * Tests for refreshing a plan via its entry in the runtime artifact model
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class RAMPlanRefreshTests extends AbstractRAMIntegrationTests {
    
    @Test
    public void refreshNotSupported() throws DeploymentException {
        DeploymentIdentity deployed = this.deployer.deploy(new File("src/test/resources/ram-plan-refresh/test.plan").toURI());
        ManageableArtifact manageableArtifact = getManageableArtifact(deployed, new StubRegion("global"));
        assertFalse(manageableArtifact.refresh());        
    }
}
