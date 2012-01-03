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
import static org.eclipse.virgo.kernel.deployer.test.PlanDeploymentTests.getInstalledBsns;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.junit.Test;
import org.osgi.framework.Bundle;

// TODO 1c. (transitive dependencies) (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
public class PlanDeploymentWithDAGTests extends AbstractDeployerIntegrationTest {

    private static final String BUNDLE_ONE_SYMBOLIC_NAME = "simple.bundle.one";

    @Test
    // 1a. (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void planReferencingAnAlreadyInstalledBundle() throws Exception {
        
        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

    @Test
    // 1a. (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void planReferencingAnAlreadyInstalledBundleUndeployBundleFirst() throws Exception {
        
        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertNoDuplicatesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
        
        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

    private void assertNoDuplicatesInstalled(Bundle[] bundles, String bundleOneSymbolicName) {
        List<String> installedBsns = getInstalledBsns(bundles);
        int found = 0;
        for (String installedBsn : installedBsns) {
            if (installedBsn.contains(bundleOneSymbolicName)) {
                found++;
            }
        }
        assertTrue("Too many bundles '" + bundleOneSymbolicName + "' found. Should be shared.", found <= 1);
    }

    @Test
    // 1b. (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void twoPlansReferencingASharedBundle() throws Exception {

        DeploymentIdentity deploymentIdentityPlanA = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        DeploymentIdentity deploymentIdentityPlanB = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicB.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentityPlanB);

        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentityPlanA);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

    @Test
    // 2. (1a. / 1b) (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365034)
    public void bundleAlreadyInstalledAsPartOfAPlan() throws Exception {

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(new File("src/test/resources/testunscopednonatomicA.plan").toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        File file = new File("src/test/resources/plan-deployment/simple.bundle.one.jar");
        DeploymentIdentity deploymentId = this.deployer.deploy(file.toURI());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentId.getType(), deploymentId.getSymbolicName(), deploymentId.getVersion());
        assertBundlesInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);

        this.deployer.undeploy(deploymentIdentity);
        assertBundlesNotInstalled(this.context.getBundles(), BUNDLE_ONE_SYMBOLIC_NAME);
    }

}
