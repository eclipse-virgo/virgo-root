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

package org.eclipse.virgo.repository.internal.persistence;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
 * <p>
 * Stub impl of ArtefactPersister with util methods to inspect its use
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is Threadsafe
 * 
 */
public class StubArtifactDescriptorPersister implements ArtifactDescriptorPersister {

    private Set<RepositoryAwareArtifactDescriptor> artefactsRespond = new HashSet<RepositoryAwareArtifactDescriptor>();

    private Set<RepositoryAwareArtifactDescriptor> artefactsRecieve = new HashSet<RepositoryAwareArtifactDescriptor>();

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> loadArtifacts() {
        return this.artefactsRespond;
    }

    /**
     * {@inheritDoc}
     */
    public void persistArtifactDescriptors(Set<RepositoryAwareArtifactDescriptor> artefacts) {
        this.artefactsRecieve = new HashSet<RepositoryAwareArtifactDescriptor>(artefacts);
    }

    // TEST SUPPORT METHODS

    public Set<RepositoryAwareArtifactDescriptor> getLastPersisted() {
        return this.artefactsRecieve;
    }

    public void addArtefact(RepositoryAwareArtifactDescriptor artefact) {
        this.artefactsRespond.add(artefact);
    }

    public void reset() {
        this.artefactsRecieve.clear();
        this.artefactsRespond.clear();
    }

}
