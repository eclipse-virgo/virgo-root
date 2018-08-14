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

import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;

/**
 * Test bundle state when listening to deployer lifecycle events.
 * 
 */
public class BundleLifecycleTests extends AbstractDeployerIntegrationTest {

    @Before
    public void setUp() {
        this.context.registerService(InstallArtifactLifecycleListener.class.getName(), new Listener(), null);
    }

    @Test public void testDeployer() throws Exception {
        File file = new File("src/test/resources/dummy.jar");

        DeploymentIdentity deploymentIdentity = this.deployer.deploy(file.toURI());
        

        this.deployer.undeploy(deploymentIdentity);
        
    }
    
    private class Listener extends InstallArtifactLifecycleListenerSupport {

        @Override
        public void onInstalled(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.INSTALLED);
        }

        @Override
        public void onInstalling(InstallArtifact installArtifact) {
            assertNoBundle(installArtifact);
        }

        @Override
        public void onResolved(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.RESOLVED);            
        }

        @Override
        public void onResolving(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.INSTALLED);
        }

        @Override
        public void onStarted(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.ACTIVE);
        }

        @Override
        public void onStarting(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.STARTING);
        }

        @Override
        public void onStopped(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.RESOLVED);
        }

        @Override
        public void onStopping(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.STOPPING);
        }

        @Override
        public void onUninstalled(InstallArtifact installArtifact) {
            assertNoBundle(installArtifact);
        }

        @Override
        public void onUninstalling(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.RESOLVED);
        }

        @Override
        public void onUnresolved(InstallArtifact installArtifact) {
            assertBundleState(installArtifact, Bundle.INSTALLED);
        }
    }

    private void assertBundleState(InstallArtifact installArtifact, int expectedBundleState) {
        Assert.assertTrue(installArtifact instanceof BundleInstallArtifact);
        BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact)installArtifact;
        Bundle bundle = bundleInstallArtifact.getBundle();
        Assert.assertNotNull(bundle);
        Assert.assertEquals(expectedBundleState, bundle.getState());
    }
    
    private void assertNoBundle(InstallArtifact installArtifact) {
        Assert.assertTrue(installArtifact instanceof BundleInstallArtifact);
        BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact)installArtifact;
        Bundle bundle = bundleInstallArtifact.getBundle();
        Assert.assertNull(bundle);
    }
}
