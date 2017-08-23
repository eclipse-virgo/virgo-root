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

/**
 * <p>
 * Implementations of this interface should have knowledge of how to read a specific type of artifact and, from it,
 * generate an {link ArtifactDescriptor}.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 */
public interface ArtifactBridge {

    /**
     * A well known key name for storing hash attributes of artifacts.
     */
    String HASH_KEY = "hash";

    /**
     * A well known key name for storing hash algorithm attributes of artifacts.
     */
    String ALGORITHM_KEY = "hash-algorithm";

    /**
     * Read the supplied <code>artifactFile</code> and builds an <code>ArtifactDescriptor</code> based upon it. It is
     * recommended that the implementation expose public static fields for the possible <code>Attribute</code> headers
     * it may create.
     * <p />
     * 
     * An implementation should return <code>null</code> if the supplied artifact is of no interest to it. If the
     * implementation identifies an artifact as being of interest to it but it is unable to build an
     * <code>ArtifactDescriptor</code> from it, for example because the artifact is corrupt or ill-formed, an
     * <code>ArtifactGenerationException</code> should be thrown.
     * 
     * @param artifactFile The artifact file from which the descriptor is to be extracted
     * @return the new <code>ArtifactDescriptor</code>, or <code>null</code> if the <code>artifactFile</code> is not
     *         understood
     * @throws ArtifactGenerationException if the provided <code>artifactFile</code> is of interest but an
     *         <code>ArtifactDescriptor</code> cannot be created from it.
     */
    ArtifactDescriptor generateArtifactDescriptor(File artifactFile) throws ArtifactGenerationException;

}
