/*******************************************************************************
 * Copyright (c) 2011 EclipseSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.test;

import static org.eclipse.virgo.kernel.deployer.test.PlanDeploymentTests.assertBundlesInstalled;
import static org.eclipse.virgo.kernel.deployer.test.PlanDeploymentTests.assertBundlesNotInstalled;

import java.io.File;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Ignore;
import org.junit.Test;

public class PlanDeploymentWithNestedDAGTests extends AbstractDeployerIntegrationTest {

    private static final String BUNDLE_ONE_SYMBOLIC_NAME = "simple.bundle.one";

    @Test
    // 3. (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void testNestedPlanWithSharedBundle() throws Exception {

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicNested.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
        // TODO - check that the bundle is installed once?!

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

}
