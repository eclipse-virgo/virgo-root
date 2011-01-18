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


import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;

/**
 * {@link ConfigInstallArtifact} is an {@link InstallArtifact} for a configuration properties file.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class ConfigInstallArtifact extends AbstractInstallArtifact {

    private final StartEngine startEngine;

    private final RefreshEngine refreshEngine;

    private final StopEngine stopEngine;

    /**
     * @throws DeploymentException  
     */
    ConfigInstallArtifact(@NonNull ArtifactIdentity identity, 
    			@NonNull ArtifactStorage artifactStorage, 
    			@NonNull StartEngine startEngine,
    			@NonNull RefreshEngine refreshEngine, 
    			@NonNull StopEngine stopEngine, 
    			@NonNull ArtifactStateMonitor artifactStateMonitor,
    			String repositoryName, 
    			EventLogger eventLogger) throws DeploymentException {
        super(identity, artifactStorage, artifactStateMonitor, repositoryName, eventLogger);

        this.startEngine = startEngine;
        this.refreshEngine = refreshEngine;
        this.stopEngine = stopEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop() throws DeploymentException {
        this.stopEngine.stop(getIdentity(), getArtifactFS());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doRefresh() throws DeploymentException {
        this.refreshEngine.refresh(getIdentity(), getArtifactFS());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doUninstall() throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void doStart(AbortableSignal signal) throws DeploymentException {
        try {
            this.startEngine.start(getIdentity(), getArtifactFS());
            signalSuccessfulCompletion(signal);
        } catch (DeploymentException e) {
            signalFailure(signal, e);
            throw e;
        } catch (RuntimeException e) {
            signalFailure(signal, e);
            throw e;
        }
    }

}
