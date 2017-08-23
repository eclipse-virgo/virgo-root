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

package org.eclipse.virgo.repository.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
 * <p>
 * The <code>ArtifactDescriptorDepository</code> is an internal interface for the actual store of Artifacts in the repository. It
 * allows the Repository itself to be independent of storage, indexing and search algorithms.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be Thread Safe
 * 
 */
public interface ArtifactDescriptorDepository {

    /**
     * Obtain a simple count of the number of Artefacts in the Depository. The count should be checked in isolation to
     * any updates to the Depository.
     * 
     * @return int count of Artefacts
     */
    int getArtifactDescriptorCount();

    /**
     * Add a new artefact to the Depository
     * 
     * @param artifactDesc
     * @throws DuplicateArtifactException if an Artefact of the same name, type and version or URI already exists in the
     *         Depository
     */
    void addArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDesc) throws DuplicateArtifactException;

    /**
     * Remove the Artefact identified by the given <code>URI</code>
     * 
     * @param uri
     * @return the descriptor of the removed Artefact, or null if no artefact found by that URI.
     */
    RepositoryAwareArtifactDescriptor removeArtifactDescriptor(URI uri);

    /**
     * Remove the supplied <code>ArtifactDescriptor</code> from the depository.
     * 
     * @param artifactDescriptor The <code>ArtifactDescriptor</code> to remove from the depository
     * @return <code>true</code> if the <code>ArtifactDescriptor</code> was found and remove, otherwise
     *         <code>false</code>.
     */
    boolean removeArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDescriptor);

    /**
     * Return all stored Artefacts that match the provided set of filters.
     * 
     * @param filters the {@link Attribute Attributes} to filter on. Can be null.
     * @return <code>Set</code> of matching Artefacts
     */
    Set<RepositoryAwareArtifactDescriptor> resolveArtifactDescriptors(Set<Attribute> filters);

    /**
     * Request that the Depository store its current state to the configured location.
     * 
     * @throws IOException
     * 
     */
    void persist() throws IOException;

}
