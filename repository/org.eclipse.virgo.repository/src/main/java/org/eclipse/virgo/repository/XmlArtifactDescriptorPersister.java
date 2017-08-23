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

package org.eclipse.virgo.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.codec.RepositoryCodec;
import org.eclipse.virgo.repository.internal.DelegatingRepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.IdentityUriMapper;
import org.eclipse.virgo.util.io.IOUtils;
import org.eclipse.virgo.util.io.PathReference;

/**
 * <p>
 * An implementation of {@link ArtifactDescriptorPersister} that will convert a collection of 
 * {@link ArtifactDescriptor}s to and from a (binary) file. The file is supplied on construction and is immutable.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * This class is thread-safe.
 * 
 */
public final class XmlArtifactDescriptorPersister implements ArtifactDescriptorPersister {

    private final RepositoryCodec codec;

    private final String repositoryName;

    private final File persistenceFile;

    /**
     * @param codec De/Serialiser
     * @param repositoryName local name of repository persisted
     * @param persistenceFile where to persist the repository
     */
    public XmlArtifactDescriptorPersister(RepositoryCodec codec, String repositoryName, File persistenceFile) {
        this.codec = codec;
        this.repositoryName = repositoryName;
        this.persistenceFile = persistenceFile;
    }

    /**
     * {@inheritDoc}
     */
    public void persistArtifactDescriptors(Set<RepositoryAwareArtifactDescriptor> artifacts) throws IOException {
        OutputStream stream = null;
        try {
            if (!this.persistenceFile.exists()) {
                createPersistenceFile();
            }
            stream = new FileOutputStream(this.persistenceFile);
            this.codec.write(new HashSet<ArtifactDescriptor>(artifacts), stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void createPersistenceFile() {
        PathReference pr = new PathReference(this.persistenceFile);
        pr.getParent().createDirectory();
        pr.createFile();
    }

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> loadArtifacts() throws IndexFormatException {
        if (!this.persistenceFile.exists()) {
            return new HashSet<RepositoryAwareArtifactDescriptor>();
        }
        InputStream stream = null;
        try {
            stream = new FileInputStream(this.persistenceFile);
            IdentityUriMapper mapper = new IdentityUriMapper();
            HashSet<RepositoryAwareArtifactDescriptor> artifacts = new HashSet<RepositoryAwareArtifactDescriptor>();
            for (ArtifactDescriptor descriptor : this.codec.read(stream)) {
                artifacts.add(new DelegatingRepositoryAwareArtifactDescriptor(descriptor, this.repositoryName, mapper));
            }
            return artifacts;
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
