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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
 * An implementation of {@link ArtifactDescriptorPersister} that does nothing. Allows the use of the standard repository
 * hierarchy without any persistence.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class NoOpArtifactDescriptorPersister implements ArtifactDescriptorPersister {

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> loadArtifacts() throws IndexFormatException {
        return new HashSet<RepositoryAwareArtifactDescriptor>();
    }

    /**
     * {@inheritDoc}
     */
    public void persistArtifactDescriptors(Set<RepositoryAwareArtifactDescriptor> descriptors) throws IOException {
    }

}
