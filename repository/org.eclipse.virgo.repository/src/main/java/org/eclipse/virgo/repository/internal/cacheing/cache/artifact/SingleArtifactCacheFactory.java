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

import java.io.File;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;


/**
 * {@link SingleArtifactCacheFactory} creates and reuses {@link SingleArtifactCache ArtifactCaches}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface SingleArtifactCacheFactory {

    /**
     * Creates, if necessary, and gets the {@link SingleArtifactCache} for the given {@link ArtifactDescriptor} using the
     * given repository cache directory to cache artifacts.
     * @param artifactDescriptor identifying the artifact cache
     * @param repositoryCacheDirectory where to cache artifacts
     * @return an artifact cache
     */
    SingleArtifactCache getArtifactCache(RepositoryAwareArtifactDescriptor artifactDescriptor, File repositoryCacheDirectory);

}
