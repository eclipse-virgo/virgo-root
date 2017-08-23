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

package org.eclipse.virgo.kernel.deployer.core;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;


public final class StubInstallArtifactLifecycleListener implements InstallArtifactLifecycleListener {

    private final AtomicInteger installingCount = new AtomicInteger();

    private final AtomicInteger installFailedCount = new AtomicInteger();

    private final AtomicInteger installedCount = new AtomicInteger();

    private final AtomicInteger resolvingCount = new AtomicInteger();

    private final AtomicInteger resolveFailedCount = new AtomicInteger();

    private final AtomicInteger resolvedCount = new AtomicInteger();

    private final AtomicInteger startingCount = new AtomicInteger();

    private final AtomicInteger startFailedCount = new AtomicInteger();
    
    private final AtomicInteger startAbortedCount = new AtomicInteger();
    
    private final AtomicInteger startedCount = new AtomicInteger();

    private final AtomicInteger stoppingCount = new AtomicInteger();

    private final AtomicInteger stopFailedCount = new AtomicInteger();

    private final AtomicInteger stoppedCount = new AtomicInteger();
    
    private final AtomicInteger unresolvedCount = new AtomicInteger();

    private final AtomicInteger uninstallingCount = new AtomicInteger();

    private final AtomicInteger uninstallFailedCount = new AtomicInteger();

    private final AtomicInteger uninstalledCount = new AtomicInteger();

    public void assertLifecycleCounts(int starting, int started, int stopping, int stopped) {
        assertStartingCount(starting);
        assertStartedCount(started);
        assertStoppingCount(stopping);
        assertStoppedCount(stopped);
    }

    public void assertLifecycleCounts(int installing, int installFailed, int installed, int resolving, int resolveFailed, int resolved, int starting,
        int startFailed, int started, int stopping, int stopFailed, int stopped, int uninstalling, int uninstallFailed, int uninstalled) {
        assertInstallingCount(installing);
        assertInstallFailedCount(installFailed);
        assertInstalledCount(installed);

        assertResolvingCount(resolving);
        assertResolvedFailedCount(resolveFailed);
        assertResolvedCount(resolved);

        assertStartingCount(starting);
        assertStartFailedCount(startFailed);
        assertStartedCount(started);

        assertStoppingCount(stopping);
        assertStopFailedCount(stopFailed);
        assertStoppedCount(stopped);

        assertUninstallingCount(uninstalling);
        assertUninstallFailedCount(uninstallFailed);
        assertUninstalledCount(uninstalled);
    }

    public void assertInstallingCount(int installing) {
        assertEquals("Incorrect installing count", installing, this.installingCount.get());
    }

    public void assertInstallFailedCount(int installFailed) {
        assertEquals("Incorrect install failed count", installFailed, this.installFailedCount.get());
    }

    public void assertInstalledCount(int installed) {
        assertEquals("Incorrect installed count", installed, this.installedCount.get());
    }

    public void assertResolvingCount(int resolving) {
        assertEquals("Incorrect resolving count", resolving, this.resolvingCount.get());
    }

    public void assertResolvedFailedCount(int resolveFailed) {
        assertEquals("Incorrect resolve failed count", resolveFailed, this.resolveFailedCount.get());
    }

    public void assertResolvedCount(int resolved) {
        assertEquals("Incorrect resolved count", resolved, this.resolvedCount.get());
    }

    public void assertStartingCount(int starting) {
        assertEquals("Incorrect starting count", starting, this.startingCount.get());
    }

    public void assertStartFailedCount(int startFailed) {
        assertEquals("Incorrect start failed count", startFailed, this.startFailedCount.get());
    }

    public void assertStartAbortedCount(int startAborted) {
        assertEquals("Incorrect start abortion count", startAborted, this.startAbortedCount.get());
    }

    public void assertStartedCount(int started) {
        assertEquals("Incorrect started count", started, this.startedCount.get());
    }

    public void assertStoppingCount(int stopping) {
        assertEquals("Incorrect stopping count", stopping, this.stoppingCount.get());
    }

    public void assertStopFailedCount(int stopFailed) {
        assertEquals("Incorrect stop failed count", stopFailed, this.stopFailedCount.get());
    }

    public void assertStoppedCount(int stopped) {
        assertEquals("Incorrect stopped count", stopped, this.stoppedCount.get());
    }
    
    public void assertUnresolvedCount(int unresolved) {
        assertEquals("Incorrect unresolved count", unresolved, this.unresolvedCount.get());
    }

    public void assertUninstallingCount(int uninstalling) {
        assertEquals("Incorrect uninstalling count", uninstalling, this.uninstallingCount.get());
    }

    public void assertUninstallFailedCount(int uninstallFailed) {
        assertEquals("Incorrect uninstall failed count", uninstallFailed, this.uninstallFailedCount.get());
    }

    public void assertUninstalledCount(int uninstalled) {
        assertEquals("Incorrect uninstalled count", uninstalled, this.uninstalledCount.get());
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalling(InstallArtifact installArtifact) {
        this.installingCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onInstallFailed(InstallArtifact installArtifact) {
        this.installFailedCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalled(InstallArtifact installArtifact) {
        this.installedCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onResolving(InstallArtifact installArtifact) {
        this.resolvingCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onResolveFailed(InstallArtifact installArtifact) {
        this.resolveFailedCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onResolved(InstallArtifact installArtifact) {
        this.resolvedCount.incrementAndGet();
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
        this.startFailedCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onStartAborted(InstallArtifact installArtifact) {
        this.startAbortedCount.incrementAndGet();
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
        this.stopFailedCount.incrementAndGet();
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
        this.unresolvedCount.incrementAndGet();
    }
    
    /**
     * {@inheritDoc}
     */
    public void onUninstalling(InstallArtifact installArtifact) {
        this.uninstallingCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstallFailed(InstallArtifact installArtifact, Throwable cause) {
        this.uninstallFailedCount.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalled(InstallArtifact installArtifact) {
        this.uninstalledCount.incrementAndGet();
    }

}
