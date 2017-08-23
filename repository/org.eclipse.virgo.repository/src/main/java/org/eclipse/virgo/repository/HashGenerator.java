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

import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;


/**
 * Create a hash from a file on the file system. The hash algorithm is implementation dependendent.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be threadsafe
 * 
 */
public interface HashGenerator {

    /**
     * Generates a hash for a given file and adds both the {@link ArtifactBridge#HASH_KEY} and
     * {@link ArtifactBridge#ALGORITHM_KEY} attributes to the {@link ArtifactDescriptor}
     * 
     * @param artifactDescriptorBuilder The descriptor builder to add attributes to
     * @param artifactFile The file to generate the hash for
     */
    void generateHash(ArtifactDescriptorBuilder artifactDescriptorBuilder, File artifactFile);
}
