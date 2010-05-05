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

package org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash;

/**
 * {@link ArtifactDescriptorHash} is an interface for the hash which may be associated with an
 * {@link org.eclipse.virgo.repository.ArtifactDescriptor ArtifactDescriptor}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface ArtifactDescriptorHash {

    /**
     * Returns <code>true</code> if and only if the {@link org.eclipse.virgo.repository.ArtifactDescriptor ArtifactDescriptor} has an associated hash.
     * 
     * @return a boolean which is <code>true</code> if and only if there is an associated hash
     */
    boolean isPresent();

    /**
     * Returns <code>true</code> if and only if the {@link org.eclipse.virgo.repository.ArtifactDescriptor ArtifactDescriptor} has an associated hash equal to the given hash.
     * 
     * @param hashToMatch the hash to compare with the associated hash
     * @return <code>true</code> if and only if the hashes match
     */
    boolean matches(String hashToMatch);
    
    /**
     * Returns the digest algorithm name, such as "sha" or "MD5", used to create the hash associated with the {@link org.eclipse.virgo.repository.ArtifactDescriptor ArtifactDescriptor}.
     * If the <code>ArtifactDescriptor</code> does not specify the algorithm, returns "sha".
     * 
     * @return the digest algorithm name
     */
    String getDigestAlgorithm();

}
