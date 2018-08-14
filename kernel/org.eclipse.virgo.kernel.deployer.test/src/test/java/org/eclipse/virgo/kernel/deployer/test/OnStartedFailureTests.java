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

import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;
import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
import org.eclipse.virgo.test.framework.ConfigLocation;


/**
 * 
 * Integration tests for handling failures from the onStarted lifecycle event.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
@Ignore
@ConfigLocation("META-INF/no.heap.dump.test.config.properties")
public class OnStartedFailureTests extends AbstractRAMIntegrationTests {
    
    private final OnStartedFailureLifecycleListener lifecycleListener = new OnStartedFailureLifecycleListener();
    
    private volatile ServiceRegistration<InstallArtifactLifecycleListener> registration;
    
    private void registerListener() {
        this.registration = this.context.registerService(InstallArtifactLifecycleListener.class, this.lifecycleListener, null);
    }
    
    private void unregisterListener() {
        ServiceRegistration<InstallArtifactLifecycleListener> localRegistration = this.registration;
        if (localRegistration != null) {
            localRegistration.unregister();
        }
    }
    
    @Test(expected=DeploymentException.class)
    public void standaloneBundleFailsDuringDeployment() throws DeploymentException {
        registerListener();
        
        try {
            this.deployer.deploy(new File("src/test/resources/onstarted-failure-tests/bundle.jar").toURI());        
        } finally {
            unregisterListener();
        }
    }
    
    @Test
    public void standaloneBundleFailsWhenStartedUsingTheRAM() throws DeploymentException {
        DeploymentIdentity deployed = this.deployer.deploy(new File("src/test/resources/onstarted-failure-tests/bundle.jar").toURI());
        
        registerListener();
        
        try {
            ManageableArtifact manageableArtifact = getManageableArtifact(deployed, new StubRegion("org.eclipse.virgo.region.user"));
            manageableArtifact.stop();
            
            try {
                manageableArtifact.start();
                fail("Start of artifact did not throw an exception");
            } catch (RuntimeException re) {
                re.printStackTrace();
            }
        } finally {
            unregisterListener();
        }
    }
    
    @Test(expected=DeploymentException.class)
    public void bundleWithinParFailsDuringDeployment() throws DeploymentException {
        registerListener();
        
        try {
            this.deployer.deploy(new File("src/test/resources/onstarted-failure-tests/started-failure.par").toURI());        
        } finally {
            unregisterListener();
        }
    }
    
    private static final class OnStartedFailureLifecycleListener extends InstallArtifactLifecycleListenerSupport {

        @Override
        public void onStarted(InstallArtifact installArtifact) throws DeploymentException {
            if (installArtifact instanceof BundleInstallArtifact) {
                throw new DeploymentException("onStarted failure");
            }
        }        
    }
}
