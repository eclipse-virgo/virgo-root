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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import org.junit.After;
import org.junit.Before;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.cacheing.CacheingRemoteRepository;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCacheFactory;
import org.eclipse.virgo.repository.internal.remote.RemoteRepository;
import org.eclipse.virgo.repository.internal.remote.RemoteRepositoryTests;


/**
 */
public class CacheingRemoteRepositoryTests extends RemoteRepositoryTests {

    private RepositoryCacheFactory mockRepositoryCacheFactory;

    private CacheingRemoteRepository cacheingRemoteRepository;

    private RepositoryCache mockRepositoryCache;

    @Before
    public void setUp() {
        this.mockRepositoryCacheFactory = createMock(RepositoryCacheFactory.class);
        this.mockRepositoryCache = createMock(RepositoryCache.class);
        expect(this.mockRepositoryCacheFactory.createRepositoryCache(isA(CacheingRemoteRepository.class))).andReturn(this.mockRepositoryCache).anyTimes();
        replay(this.mockRepositoryCacheFactory, this.mockRepositoryCache);
    }

    @After
    public void tearDown() {
        verify(this.mockRepositoryCacheFactory, this.mockRepositoryCache);
        reset(this.mockRepositoryCacheFactory, this.mockRepositoryCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RemoteRepository createRemoteRepository(RemoteRepositoryConfiguration configuration, EventLogger eventLogger) {
        this.cacheingRemoteRepository = new CacheingRemoteRepository(configuration, eventLogger, this.mockRepositoryCacheFactory);
        return this.cacheingRemoteRepository;
    }

}
