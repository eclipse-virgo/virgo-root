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

package org.eclipse.virgo.kernel.install.artifact;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;

/**
 * {@link InstallArtifactLifecycleListenerSupport} is an abstract implementation of
 * {@link InstallArtifactLifecycleListener} which ignores all events.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public abstract class InstallArtifactLifecycleListenerSupport implements InstallArtifactLifecycleListener {

    /**
     * {@inheritDoc}
     */
    public void onInstallFailed(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalled(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onInstalling(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onResolveFailed(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onResolved(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onResolving(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onStartFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onStartAborted(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onStarted(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onStarting(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onStopFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onStopped(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onStopping(InstallArtifact installArtifact) {
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstallFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalled(InstallArtifact installArtifact) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void onUninstalling(InstallArtifact installArtifact) throws DeploymentException {
    }

    /** 
     * {@inheritDoc}
     */
    public void onUnresolved(InstallArtifact installArtifact) throws DeploymentException {
    }
}
