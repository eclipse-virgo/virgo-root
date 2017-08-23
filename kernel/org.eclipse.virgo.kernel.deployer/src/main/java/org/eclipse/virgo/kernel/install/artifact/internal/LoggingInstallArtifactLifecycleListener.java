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


import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.medic.eventlog.EventLogger;

/**
 * {@link LoggingInstallArtifactLifecycleListener} listens for lifecycle events for {@link InstallArtifact
 * InstallArtifacts} and logs messages.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class LoggingInstallArtifactLifecycleListener implements InstallArtifactLifecycleListener {

    private final EventLogger eventLogger;

    public LoggingInstallArtifactLifecycleListener(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    public void onInstallFailed(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.INSTALL_FAILED, installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalled(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.INSTALLED, installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalling(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.INSTALLING, installArtifact);
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
    public void onResolving(InstallArtifact installArtifact) {

    }

    /**
     * {@inheritDoc}
     */
    public void onStartFailed(InstallArtifact installArtifact, Throwable cause) {
        logEvent(DeployerLogEvents.START_FAILED, installArtifact, cause);
    }

    /**
     * {@inheritDoc}
     */
    public void onStartAborted(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.START_ABORTED, installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    public void onStarted(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.STARTED, installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    public void onStarting(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.STARTING, installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    public void onStopFailed(InstallArtifact installArtifact, Throwable cause) {
        logEvent(DeployerLogEvents.STOP_FAILED, installArtifact, cause);
    }

    /**
     * {@inheritDoc}
     */
    public void onStopped(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.STOPPED, installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    public void onStopping(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.STOPPING, installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    public void onUnresolved(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstallFailed(InstallArtifact installArtifact, Throwable cause) {
        logEvent(DeployerLogEvents.UNINSTALL_FAILED, installArtifact, cause);
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalled(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.UNINSTALLED, installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalling(InstallArtifact installArtifact) {
        logEvent(DeployerLogEvents.UNINSTALLING, installArtifact);
    }

    private void logEvent(DeployerLogEvents event, InstallArtifact installArtifact) {
        this.eventLogger.log(event, installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion());
    }

    private void logEvent(DeployerLogEvents event, InstallArtifact installArtifact, Throwable cause) {
        if (cause == null) {
            logEvent(event, installArtifact);
        } else {
            this.eventLogger.log(event, cause, installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion());
        }
    }
}
