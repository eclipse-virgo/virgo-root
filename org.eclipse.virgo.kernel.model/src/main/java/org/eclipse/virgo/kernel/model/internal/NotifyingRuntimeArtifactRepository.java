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

package org.eclipse.virgo.kernel.model.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link RuntimeArtifactRepository} that notifies a collection of listeners that a change has
 * happened to this repository.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 * @see ArtifactRepositoryListener
 */
public final class NotifyingRuntimeArtifactRepository implements RuntimeArtifactRepository {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object monitor = new Object();

    private final Set<Artifact> artifacts = new HashSet<Artifact>();

    private final List<ArtifactRepositoryListener> listeners;

    public NotifyingRuntimeArtifactRepository(ArtifactRepositoryListener... listeners) {
        this.listeners = Arrays.asList(listeners);
    }

    /**
     * {@inheritDoc}
     */
    public boolean add(@NonNull Artifact artifact) {
        synchronized (this.monitor) {
            boolean result = this.artifacts.add(artifact);
            if (result) {
                for (ArtifactRepositoryListener listener : listeners) {
                    try {
                        listener.added(artifact);
                    } catch (Exception e) {
                        logger.error(String.format("Exception calling added() on listener '%s'", listener.toString()), e);
                    }
                }
            }
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean remove(@NonNull Artifact artifact) {
        synchronized (this.monitor) {
            boolean result = this.artifacts.remove(artifact);
            if (result) {
                for (ArtifactRepositoryListener listener : listeners) {
                    try {
                        listener.removed(artifact);
                    } catch (Exception e) {
                        logger.error(String.format("Exception calling removed() on listener '%s'", listener.toString()), e);
                    }
                }
            }
            return result;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean remove(@NonNull String type, @NonNull String name, @NonNull Version version, @NonNull Region region) {
        synchronized (this.monitor) {
            Artifact artifact = getArtifact(type, name, version, region);
            if(artifact != null){
                return remove(artifact);
            } else {
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<Artifact> getArtifacts() {
        synchronized (this.monitor) {
            return new HashSet<Artifact>(this.artifacts);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Artifact getArtifact(@NonNull String type, @NonNull String name, @NonNull Version version, @NonNull Region region) {
        synchronized (this.monitor) {
            for (Artifact artifact : this.artifacts) {
                if (artifact.getType().equals(type) && 
                    artifact.getName().equals(name) && 
                    artifact.getVersion().equals(version) &&
                    artifact.getRegion().getName().equals(region.getName())){
                    return artifact;
                }
            }
            return null;
        }
    }

}
