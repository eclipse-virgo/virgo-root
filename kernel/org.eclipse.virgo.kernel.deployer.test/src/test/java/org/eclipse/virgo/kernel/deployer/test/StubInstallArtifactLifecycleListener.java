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

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;

final class StubInstallArtifactLifecycleListener implements InstallArtifactLifecycleListener {

    private final AtomicInteger startingCount = new AtomicInteger();

    private final AtomicInteger startedCount = new AtomicInteger();

    private final AtomicInteger stoppingCount = new AtomicInteger();

    private final AtomicInteger stoppedCount = new AtomicInteger();

    public void assertLifecycleCounts(int starting, int started, int stopping, int stopped) {
        assertEquals("Incorrect starting count", starting, this.startingCount.get());
        assertEquals("Unexpected started count", started, this.startedCount.get());
        assertEquals("Unexpected stopping count", stopping, this.stoppingCount.get());
        assertEquals("Unexpected stopped count", stopped, this.stoppedCount.get());
    }

    public boolean checkLifecycleCounts(int starting, int started, int stopping, int stopped) {
        return starting == this.startingCount.get() && started == this.startedCount.get() && stopping == this.stoppingCount.get()
            && stopped == this.stoppedCount.get();
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalling(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onInstallFailed(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalled(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onResolving(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onResolveFailed(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onResolved(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onStarting(InstallArtifact installArtifact) {
        this.startingCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onStartFailed(InstallArtifact installArtifact, Throwable cause) {
    }

    /**
     * {@inheritDoc}
     */
    public void onStartAborted(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onStarted(InstallArtifact installArtifact) {
        this.startedCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onStopping(InstallArtifact installArtifact) {
        this.stoppingCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onStopFailed(InstallArtifact installArtifact, Throwable cause) {
    }

    /**
     * {@inheritDoc}
     */
    public void onStopped(InstallArtifact installArtifact) {
        this.stoppedCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onUnresolved(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalling(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstallFailed(InstallArtifact installArtifact, Throwable cause) {
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalled(InstallArtifact installArtifact) {
    }

}
