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

package org.eclipse.virgo.repository.internal.cacheing.cache.artifact;

import java.net.URI;

import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.ArtifactDescriptorHash;


/**
 * {@link SingleArtifactCache} provides a cache for a specific artifact from a specific repository.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface SingleArtifactCache {

    /**
     * Gets the URI of the cached artifact or <code>null</code> if the artifact is not cached (or is out of date).
     * 
     * @param artifactDescriptorHash the hash from the repository index
     * @return a URI or <code>null</code>
     */
    URI getCachedUri(ArtifactDescriptorHash artifactDescriptorHash);

}
