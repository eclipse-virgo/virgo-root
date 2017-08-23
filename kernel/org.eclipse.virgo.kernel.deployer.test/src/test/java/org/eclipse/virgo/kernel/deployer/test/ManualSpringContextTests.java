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

import java.io.File;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Ignore;
import org.junit.Test;


@Ignore("Failed when not connected to the internet")
public class ManualSpringContextTests extends AbstractDeployerIntegrationTest {

    @Test
    public void testManualContextDefinitionWithBadImports() throws Exception {
        DeploymentIdentity id = this.deployer.deploy(new File("src/test/resources/manual-context.jar").toURI());
        try {
            assertNotNull(id);
        } finally {
            this.deployer.undeploy(id);
        }
    }
}
