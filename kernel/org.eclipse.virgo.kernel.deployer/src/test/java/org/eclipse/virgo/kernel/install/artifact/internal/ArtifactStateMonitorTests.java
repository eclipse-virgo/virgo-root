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

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;


import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.StubInstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.internal.StandardArtifactStateMonitor;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

/**
 */
public class ArtifactStateMonitorTests {

    BundleContext bundleContext;

    ArtifactStateMonitor asm;

    StubInstallArtifactLifecycleListener listener;

    @Before
    public void setUp() {
        this.bundleContext = new StubBundleContext();
        this.asm = new StandardArtifactStateMonitor(this.bundleContext);
        this.listener = new StubInstallArtifactLifecycleListener();
        this.bundleContext.registerService(InstallArtifactLifecycleListener.class.getName(), this.listener, null);
    }

    @Test
    public void testInstalling() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onInstalling(null);
        this.listener.assertLifecycleCounts(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testInstallFailed() throws DeploymentException {
        // Set INSTALLING before failing install so state actually changes to INITIAL.
        this.asm.onInstalling(null);
        this.listener.assertLifecycleCounts(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onInstallFailed(null);
        this.listener.assertLifecycleCounts(1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testInstalled() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onInstalled(null);
        this.listener.assertLifecycleCounts(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testResolving() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onResolving(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testResolveFailed() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onResolveFailed(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testResolved() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onResolved(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testStarting() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onStarting(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testStartFailed() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onStartFailed(null, null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testStarted() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onStarted(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0);
    }
    
    @Test
    public void testStopping() {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onStopping(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0);
    }

    @Test
    public void testStopFailed() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onStopFailed(null, null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0);
    }

    @Test
    public void testStopped() {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onStopped(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    }
    
    @Test
    public void testUninstalling() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onUninstalling(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0);
    }

    @Test
    public void testUninstallFailed() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onUninstallFailed(null, null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0);
    }

    @Test
    public void testUninstalled() throws DeploymentException {
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.asm.onUninstalled(null);
        this.listener.assertLifecycleCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
    }

}
