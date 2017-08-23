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

package org.eclipse.virgo.repository.internal.cacheing.cache;

import java.io.File;

import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.StandardSingleArtifactCacheFactory;
import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.StandardArtifactDescriptorHashFactory;
import org.eclipse.virgo.repository.internal.remote.RemoteRepository;


/**
 * {@link StandardRepositoryCacheFactory} is the default implementation of {@link RepositoryCacheFactory}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public class StandardRepositoryCacheFactory implements RepositoryCacheFactory {

    /**
     * {@inheritDoc}
     */
    public RepositoryCache createRepositoryCache(RemoteRepository remoteRepository) {
        File cacheDirectory = remoteRepository.getCacheDirectory();
        return new StandardRepositoryCache(remoteRepository.getName(), cacheDirectory, new StandardSingleArtifactCacheFactory(),
            new StandardArtifactDescriptorHashFactory());
    }

}
