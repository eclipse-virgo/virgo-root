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

package org.eclipse.virgo.apps.repository.core.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.codec.RepositoryCodec;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.XmlArtifactDescriptorPersister;


/**
 * An {@link ArtifactDescriptorPersister} which allows export of the persisted index file and only generates this when export is requested.
 * It delegates to {@link XmlArtifactDescriptorPersister}s to do the actual writing. <p /> 
 *
 * <strong>Concurrent Semantics</strong><br />
 * This implementation is thread-safe.
 *
 */
class LazyExportableXMLArtifactDescriptorPersister implements ExportingArtifactDescriptorPersister {

    private final String repositoryName;
    
    private final RepositoryCodec repositoryCodec;
    
    private final Object monitorPersister = new Object(); // serialises access to following private state
        private volatile boolean currentSetPersisted;
        private final Set<RepositoryAwareArtifactDescriptor> artifactDescriptors = new HashSet<RepositoryAwareArtifactDescriptor>();
        private final FilePool indexPool;

    public LazyExportableXMLArtifactDescriptorPersister(String repositoryName, RepositoryCodec repositoryCodec, FilePool indexPool) {
        this.repositoryCodec = repositoryCodec;
        this.repositoryName = repositoryName;
        this.currentSetPersisted = false;
        this.indexPool = indexPool;
    }

    public Set<RepositoryAwareArtifactDescriptor> loadArtifacts() throws IndexFormatException {
        synchronized (monitorPersister) {
            Set<RepositoryAwareArtifactDescriptor> artifactDescriptorsCopy = new HashSet<RepositoryAwareArtifactDescriptor>(this.artifactDescriptors.size());
            artifactDescriptorsCopy.addAll(this.artifactDescriptors);
            return artifactDescriptorsCopy;
        }
    }

    public void persistArtifactDescriptors(Set<RepositoryAwareArtifactDescriptor> descriptors) throws IOException {
        synchronized (monitorPersister) {
            this.artifactDescriptors.clear();
            this.artifactDescriptors.addAll(descriptors);
            this.currentSetPersisted = false;
        }
    }
    
    public File exportIndexFile() throws IOException {
        File indexFile = null;
        synchronized (monitorPersister) {
            try {
                if (!currentSetPersisted) {
                    indexFile = this.indexPool.generateNextPoolFile();
                    ArtifactDescriptorPersister artifactDescriptorPersister = new XmlArtifactDescriptorPersister(this.repositoryCodec, this.repositoryName, indexFile);
                    artifactDescriptorPersister.persistArtifactDescriptors(this.artifactDescriptors);
                    this.indexPool.putFileInPool(indexFile);
                    this.currentSetPersisted = true;
                } else {
                    indexFile = this.indexPool.getMostRecentPoolFile();
                }
            } catch (FilePoolException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        return indexFile;
    }

}
