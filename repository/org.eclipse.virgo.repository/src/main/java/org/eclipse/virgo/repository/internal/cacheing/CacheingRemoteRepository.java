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

import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCacheFactory;
import org.eclipse.virgo.repository.internal.remote.RemoteRepository;

/**
 * {@link CacheingRemoteRepository} extends {@link RemoteRepository} by cacheing retrieved artifacts in a local disk
 * cache.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public final class CacheingRemoteRepository extends RemoteRepository {

    private final Object monitor = new Object();

    private final RepositoryCacheFactory repositoryCacheFactory;

    private RepositoryCache cache;

    public CacheingRemoteRepository(RemoteRepositoryConfiguration configuration, EventLogger eventLogger,
        RepositoryCacheFactory repositoryCacheFactory) {
        super(configuration, eventLogger);
        this.repositoryCacheFactory = repositoryCacheFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void start() {
        super.start();
        synchronized (this.monitor) {
            this.cache = this.repositoryCacheFactory.createRepositoryCache(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        synchronized (this.monitor) {
            this.cache = null;
        }
        super.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query createQuery(String key, String value) {
        Query uncachedQuery = super.createQuery(key, value);
        return createQuery(uncachedQuery);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query createQuery(String key, String value, Map<String, Set<String>> properties) {
        Query uncachedQuery = super.createQuery(key, value, properties);
        return createQuery(uncachedQuery);
    }

    private Query createQuery(Query uncachedQuery) {
        RepositoryCache repositoryCache = getRepositoryCache();
        return repositoryCache == null ? uncachedQuery : createCacheingQuery(repositoryCache, uncachedQuery);
    }
    
    private RepositoryCache getRepositoryCache() {
        synchronized (this.monitor) {
            return this.cache;
        }
    }

    // This method takes a RepositoryCache parameter rather than using the instance variable to avoid racing with stop().
    private static Query createCacheingQuery(RepositoryCache activeCache, Query uncachedQuery) {
        return new CacheingQuery(uncachedQuery, activeCache);
    }

}
