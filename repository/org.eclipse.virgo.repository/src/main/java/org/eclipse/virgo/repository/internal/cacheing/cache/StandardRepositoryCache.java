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
import java.net.URI;

import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.SingleArtifactCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.SingleArtifactCacheFactory;
import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.ArtifactDescriptorHash;
import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.ArtifactDescriptorHashFactory;
import org.eclipse.virgo.util.common.Assert;

/**
 * {@link StandardRepositoryCache} is a {@link RepositoryCache} for a given {@link Repository} which provides disk
 * cacheing.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class StandardRepositoryCache implements RepositoryCache {

    private final String repositoryName;

    private final File repositoryCacheDirectory;

    private final SingleArtifactCacheFactory artifactCacheFactory;

    private final ArtifactDescriptorHashFactory artifactDescriptorHashFactory;

    StandardRepositoryCache(String repositoryName, File cacheDirectory, SingleArtifactCacheFactory artifactCacheFactory,
        ArtifactDescriptorHashFactory artifactDescriptorHashFactory) {
        Assert.notNull(repositoryName, "repository name must not be null");
        this.repositoryName = repositoryName;

        this.repositoryCacheDirectory = createRepositoryCacheDirectory(repositoryName, cacheDirectory);

        this.artifactCacheFactory = artifactCacheFactory;

        this.artifactDescriptorHashFactory = artifactDescriptorHashFactory;
    }

    private static File createRepositoryCacheDirectory(String repositoryName, File cacheDirectory) {
        File dir = new File(cacheDirectory, repositoryName);
        if (dir.exists()) {
            if (dir.isFile()) {
                throw new RuntimeException(new RepositoryCreationException("Repository cache directory " + dir.getPath()
                    + " cannot be created as there is a file at that location"));
            }
        } else if (!dir.mkdirs()) {
            throw new RuntimeException(new RepositoryCreationException("Error creating repository cache directory " + dir.getPath()));
        }
        return dir;
    }

    /**
     * {@inheritDoc}
     */
    public URI getUri(RepositoryAwareArtifactDescriptor artifactDescriptor) {
        checkRepositoryName(artifactDescriptor);

        SingleArtifactCache artifactDescriptorCache = getArtifactCache(artifactDescriptor);
        return artifactDescriptorCache.getCachedUri(createArtifactDescriptorHash(artifactDescriptor));
    }

    private void checkRepositoryName(RepositoryAwareArtifactDescriptor artifactDescriptor) {
        Assert.isTrue(this.repositoryName.equals(artifactDescriptor.getRepositoryName()), "Wrong RepositoryCache for the given artifact descriptor");
    }

    private ArtifactDescriptorHash createArtifactDescriptorHash(RepositoryAwareArtifactDescriptor artifactDescriptor) {
        return this.artifactDescriptorHashFactory.createArtifactDescriptorHash(artifactDescriptor);
    }

    private SingleArtifactCache getArtifactCache(RepositoryAwareArtifactDescriptor artifactDescriptor) {
        return this.artifactCacheFactory.getArtifactCache(artifactDescriptor, this.repositoryCacheDirectory);
    }

}
