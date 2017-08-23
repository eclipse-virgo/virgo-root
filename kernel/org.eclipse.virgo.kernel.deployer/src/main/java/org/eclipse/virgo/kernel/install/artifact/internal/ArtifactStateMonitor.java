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

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact.State;

/**
 * 
 * Implementations must be thread safe
 * 
 */
public interface ArtifactStateMonitor {

    /**
     * Returns the current state of the install artifact according to the events which have occurred.
     * 
     * @return the {@link State} of this {@link StandardArtifactStateMonitor}
     */
    public State getState();

    public void setState(State state);

    public void onInstalling(InstallArtifact installArtifact) throws DeploymentException;

    public void onInstallFailed(InstallArtifact installArtifact) throws DeploymentException;

    public void onInstalled(InstallArtifact installArtifact) throws DeploymentException;

    public void onResolving(InstallArtifact installArtifact) throws DeploymentException;

    public void onResolveFailed(InstallArtifact installArtifact) throws DeploymentException;

    public void onResolved(InstallArtifact installArtifact) throws DeploymentException;

    public boolean onStarting(InstallArtifact installArtifact) throws DeploymentException;

    public void onStartFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException;

    public void onStartAborted(InstallArtifact installArtifact) throws DeploymentException;

    public void onStarted(InstallArtifact installArtifact) throws DeploymentException;

    public void onStopping(InstallArtifact installArtifact);

    public void onStopFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException;

    public void onStopped(InstallArtifact installArtifact);

    public void onUnresolved(InstallArtifact installArtifact) throws DeploymentException;

    public void onUninstalling(InstallArtifact installArtifact) throws DeploymentException;

    public void onUninstallFailed(InstallArtifact installArtifact, Throwable cause) throws DeploymentException;

    public void onUninstalled(InstallArtifact installArtifact) throws DeploymentException;

}