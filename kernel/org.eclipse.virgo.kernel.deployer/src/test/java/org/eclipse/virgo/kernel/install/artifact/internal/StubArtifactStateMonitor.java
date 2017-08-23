/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 copyright_holder
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    cgfrost - initial contribution
 */

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact.State;

/**
 * TODO Document StubArtifactStateMonitor
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * TODO Document concurrent semantics of StubArtifactStateMonitor
 */
public class StubArtifactStateMonitor implements ArtifactStateMonitor {

    private State myState = State.RESOLVED;
    
    /** 
     * {@inheritDoc}
     */
    public State getState() {
        return this.myState;
    }

    /** 
     * {@inheritDoc}
     */
    public void setState(State state) {
        this.myState = state;
    }

    /** 
     * {@inheritDoc}
     */
    public void onInstalling(InstallArtifact installArtifact) throws DeploymentException {

    }

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
    public void onResolving(InstallArtifact installArtifact) throws DeploymentException {

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
    public boolean onStarting(InstallArtifact installArtifact) throws DeploymentException {
        return false;
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
    public void onStopping(InstallArtifact installArtifact) {

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
    public void onUnresolved(InstallArtifact installArtifact) throws DeploymentException {

    }

    /** 
     * {@inheritDoc}
     */
    public void onUninstalling(InstallArtifact installArtifact) throws DeploymentException {

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

}
