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

package org.eclipse.virgo.kernel.model.internal.deployer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.nano.core.BlockingAbortableSignal;
import org.eclipse.virgo.nano.core.FailureSignalledException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact.State;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.internal.AbstractArtifact;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.BundleContext;

/**
 * Implementation of {@link Artifact} that delegates to a Kernel {@link InstallArtifact}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
class DeployerArtifact extends AbstractArtifact {

    private final InstallArtifact installArtifact;
    
    public DeployerArtifact(@NonNull BundleContext bundleContext, @NonNull InstallArtifact installArtifact, Region region) {
        super(bundleContext, installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(), region);
        this.installArtifact = installArtifact;
    }

    /**
     * {@inheritDoc}
     */
    public final ArtifactState getState() {
        return mapInstallArtifactState(this.installArtifact.getState());
    }

    /**
     * {@inheritDoc}
     */
    public final void start() {
        try {
            BlockingAbortableSignal signal = new BlockingAbortableSignal();
            this.installArtifact.start(signal);
            try {
                if (!signal.awaitCompletion(5, TimeUnit.MINUTES)) {
                	if(signal.isAborted()){
                		throw new RuntimeException("Started aborted");
                	} else {
                		throw new RuntimeException("Started failed");
                	}
                }
            } catch (FailureSignalledException fse) {
                throw new RuntimeException(fse.getCause());
            }
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void stop() {
        try {
            this.installArtifact.stop();
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void uninstall() {
        try {
            this.installArtifact.uninstall();
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final boolean refresh() {
        try {
            return this.installArtifact.refresh();
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getProperties() {
        Set<String> propertyNames = this.installArtifact.getPropertyNames();
        Map<String, String> result = new HashMap<String, String>(propertyNames.size());
        for (String propertyName : propertyNames) {
            String propertyValue = this.installArtifact.getProperty(propertyName);
            // The property may have been deleted concurrently.
            if (propertyValue != null) {
                result.put(propertyName, propertyValue);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private ArtifactState mapInstallArtifactState(State state) {
        if (State.INITIAL == state) {
            return ArtifactState.INITIAL;
        } else if (State.INSTALLING == state) {
            return ArtifactState.INSTALLING;
        } else if (State.INSTALLED == state) {
            return ArtifactState.INSTALLED;
        } else if (State.RESOLVING == state) {
            return ArtifactState.RESOLVING;
        } else if (State.RESOLVED == state) {
            return ArtifactState.RESOLVED;
        } else if (State.STARTING == state) {
            return ArtifactState.STARTING;
        } else if (State.ACTIVE == state) {
            return ArtifactState.ACTIVE;
        } else if (State.STOPPING == state) {
            return ArtifactState.STOPPING;
        } else if (State.UNINSTALLING == state) {
            return ArtifactState.UNINSTALLING;
        } else {
            return ArtifactState.UNINSTALLED;
        }
    }
}
