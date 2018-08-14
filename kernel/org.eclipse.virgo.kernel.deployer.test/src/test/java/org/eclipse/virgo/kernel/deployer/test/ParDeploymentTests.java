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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;

// TODO update the test bundles to a newer version of javax.servlet and reactivate @Ignored test
public class ParDeploymentTests extends AbstractDeployerIntegrationTest {

    private static final File PAR_FILE = new File("src/test/resources/BundlesAndConfig.par");

    private static final File PAR_FOR_BUG331767 = new File("src/test/resources/bug331767.par");

    private static final File PAR_FOR_BUG330506 = new File("src/test/resources/bug330506.par");

    private static final File PAR_CONTAINING_PLAN = new File("src/test/resources/plan-in-par/ParContainingPlan.par");

    private static final String BUNDLE_SYMBOLIC_NAME = "appA-1-bundleA";

    private static final String BUNDLE_SYMBOLIC_NAME_BUG331767 = "PARbug331767-1-BUNDLEbug331767";

    private static final Version BUNDLE_VERSION = new Version(1, 0, 0);

    @Test
    @Ignore("missing constraint: <Import-Package: javax.servlet; version=\"[2.5.0,3.0.0)\">")
    public void deployParContainingBundlesAndProperties() throws DeploymentException, IOException, InvalidSyntaxException {
        DeploymentIdentity deploymentIdentity = this.deployer.deploy(PAR_FILE.toURI());

        Configuration configuration = getConfiguration("foo");
        assertNotNull(configuration);
        assertEquals("bar", configuration.getProperties().get("foo"));

        assertBundlePresent(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);

        this.deployer.undeploy(deploymentIdentity);

        configuration = getConfiguration("foo");
        assertNull(configuration);

        assertBundleNotPresent(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);
    }

    @Test
    public void deployParContainingDynamicImportStar() throws DeploymentException {
        DeploymentIdentity deploymentIdentity = this.deployer.deploy(PAR_FOR_BUG331767.toURI());
        assertBundlePresent(BUNDLE_SYMBOLIC_NAME_BUG331767, BUNDLE_VERSION);
        this.deployer.undeploy(deploymentIdentity);
        assertBundleNotPresent(BUNDLE_SYMBOLIC_NAME_BUG331767, BUNDLE_VERSION);
    }

    @Test(expected = DeploymentException.class)
    public void deployParContainingFragmentOfSystemBundle() throws DeploymentException {
        this.deployer.deploy(PAR_FOR_BUG330506.toURI());
    }

    @Test
    @Ignore("missing constraint: <Import-Package: javax.servlet; version=\"[2.5.0,3.0.0)\">")
    public void deployParContainingPlan() throws DeploymentException {
        DeploymentIdentity deploymentIdentity = this.deployer.deploy(PAR_CONTAINING_PLAN.toURI());
        assertBundlePresent("par.with.plan-1-simple.bundle.one", new Version(TEST_APPS_VERSION));
        this.deployer.undeploy(deploymentIdentity);
    }

}
