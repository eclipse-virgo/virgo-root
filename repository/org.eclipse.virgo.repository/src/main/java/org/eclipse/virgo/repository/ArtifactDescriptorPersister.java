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

import java.io.IOException;
import java.util.Set;


/**
 * <p>
 * Implementations of this interface are able to store and load Artifact meta data. The location where the data should
 * be stored is to be specified in an implementation specific way.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations should be Threadsafe
 * 
 */
public interface ArtifactDescriptorPersister {

    /**
     * Persist the provided set of {@link RepositoryAwareArtifactDescriptor}s to a permanent medium.
     * 
     * @param descriptors
     * 
     * @throws IOException
     */
    void persistArtifactDescriptors(Set<RepositoryAwareArtifactDescriptor> descriptors) throws IOException;

    /**
     * Retrieve the artifacts already stored. If no {@link RepositoryAwareArtifactDescriptor}s are stored then the empty set should be returned.
     * 
     * @return set of ArtifactDescriptors of artifacts stored.
     * @throws IndexFormatException
     */
    Set<RepositoryAwareArtifactDescriptor> loadArtifacts() throws IndexFormatException;
}
