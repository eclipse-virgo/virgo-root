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
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.cacheing.CacheingArtifactDescriptor;
import org.eclipse.virgo.repository.internal.cacheing.CacheingQuery;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCache;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class CacheingQueryTests {

    private URI testURI;


    private CacheingQuery cacheingQuery;

    private Query mockQuery;

    private RepositoryCache mockRepositoryCache;

    @Before
    public void setUp() throws Exception {
        try {
            testURI = new URI("some://uri");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.mockQuery = createMock(Query.class);
        this.mockRepositoryCache = createMock(RepositoryCache.class);
        this.cacheingQuery = new CacheingQuery(this.mockQuery, this.mockRepositoryCache);
    }

    @Test
    public void testAddFilterStringString() {
        expect(this.mockQuery.addFilter("", "")).andReturn(null);
        replay(this.mockQuery);
        this.cacheingQuery.addFilter("", "");
        verify(this.mockQuery);
    }

    @Test
    public void testAddFilterStringStringMapOfStringSetOfString() {
        expect(this.mockQuery.addFilter("", null)).andReturn(null);
        replay(this.mockQuery);
        this.cacheingQuery.addFilter("", null);
        verify(this.mockQuery);
    }

    @Test
    public void testRun() {

        Set<RepositoryAwareArtifactDescriptor> set = new HashSet<RepositoryAwareArtifactDescriptor>();
        RepositoryAwareArtifactDescriptor raad = createMock(RepositoryAwareArtifactDescriptor.class);
        set.add(raad);
        expect(this.mockQuery.run()).andReturn(set);
        expect(this.mockRepositoryCache.getUri(isA(CacheingArtifactDescriptor.class))).andReturn(testURI);
        replay(this.mockQuery, this.mockRepositoryCache, raad);

        Set<RepositoryAwareArtifactDescriptor> raadSet = this.cacheingQuery.run();
        assertEquals(1, raadSet.size());
        
        RepositoryAwareArtifactDescriptor craad = raadSet.iterator().next();
        assertEquals(testURI, craad.getUri());

        verify(this.mockQuery, this.mockRepositoryCache, raad);
    }

}
