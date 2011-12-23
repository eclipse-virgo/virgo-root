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

package org.eclipse.virgo.repository.internal.cacheing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCache;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * {@link CacheingQuery} wraps a {@link Query} and provides cacheing in the run method.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class CacheingQuery implements Query {

    private final Query delegate;

    private final RepositoryCache cache;

    public CacheingQuery(Query delegate, RepositoryCache cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    /**
     * {@inheritDoc}
     */
    public Query addFilter(String name, String value) {
        this.delegate.addFilter(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Query addFilter(String name, String value, Map<String, Set<String>> properties) {
        this.delegate.addFilter(name, value, properties);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setVersionRangeFilter(VersionRange versionRange) {
        this.delegate.setVersionRangeFilter(versionRange);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query setVersionRangeFilter(VersionRange versionRange, VersionRangeMatchingStrategy strategy) {
        this.delegate.setVersionRangeFilter(versionRange, strategy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Set<RepositoryAwareArtifactDescriptor> run() {
        Set<RepositoryAwareArtifactDescriptor> descriptors = this.delegate.run();

        Set<RepositoryAwareArtifactDescriptor> cacheingDescriptors = new HashSet<RepositoryAwareArtifactDescriptor>(descriptors.size());
        for (RepositoryAwareArtifactDescriptor descriptor : descriptors) {
            cacheingDescriptors.add(new CacheingArtifactDescriptor(descriptor, this.cache));
        }

        return Collections.unmodifiableSet(cacheingDescriptors);
    }

}
