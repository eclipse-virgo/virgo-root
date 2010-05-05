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
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.cacheing.CacheingArtifactDescriptor;
import org.osgi.framework.Version;


/**
 * {@link StandardSingleArtifactCacheFactory} is the default implementation of {@link SingleArtifactCacheFactory}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public class StandardSingleArtifactCacheFactory implements SingleArtifactCacheFactory {

    private static final String CACHE_NAME_SEPARATOR = ":";

    /**
     * Artifact caches are indexed by a combination of repository name and artifact URI. Operations on the files in an
     * artifact cache are serialized via the artifact cache, so it is essential that multiple threads operating on the
     * same files do so via the same artifact cache instance. This is implemented by ensuring that the map below
     * increases monotonically. That is, artifact caches are never removed and re-added.
     */
    private final ConcurrentMap<String, SingleArtifactCache> artifactCaches = new ConcurrentHashMap<String, SingleArtifactCache>();

    /**
     * {@inheritDoc}
     */
    public SingleArtifactCache getArtifactCache(RepositoryAwareArtifactDescriptor artifactDescriptor, File repositoryCacheDirectory) {
        URI remoteUri = getRemoteURI(artifactDescriptor);
        return getArtifactCache(artifactDescriptor.getRepositoryName(), artifactDescriptor.getType(), artifactDescriptor.getName(),
            artifactDescriptor.getVersion(), remoteUri, artifactDescriptor.getFilename(), repositoryCacheDirectory);
    }

    private URI getRemoteURI(RepositoryAwareArtifactDescriptor artifactDescriptor) {
        URI remoteUri;
        if (artifactDescriptor instanceof CacheingArtifactDescriptor) {
            CacheingArtifactDescriptor cacheingArtifactDescriptor = (CacheingArtifactDescriptor) artifactDescriptor;
            remoteUri = cacheingArtifactDescriptor.getRemoteUri();
        } else {
            remoteUri = artifactDescriptor.getUri();
        }
        return remoteUri;
    }

    private SingleArtifactCache getArtifactCache(String repositoryName, String type, String name, Version version, URI uri, String fileName,
        File repositoryCacheDirectory) {
        String cacheName = getCacheName(repositoryName, uri);
        SingleArtifactCache cache = this.artifactCaches.get(cacheName);
        if (cache == null) {
            this.artifactCaches.putIfAbsent(cacheName, new StandardSingleArtifactCache(type, name, version, uri, fileName, repositoryCacheDirectory));
            cache = this.artifactCaches.get(cacheName);
        }
        return cache;
    }

    private String getCacheName(String repositoryName, URI uri) {
        return repositoryName + CACHE_NAME_SEPARATOR + uri.toString();
    }

}
