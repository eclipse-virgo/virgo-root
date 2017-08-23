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

import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.cacheing.CacheingArtifactDescriptor;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCache;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class CacheingArtifactDescriptorTests {

    private CacheingArtifactDescriptor cacheingArtifactDescriptor;

    private RepositoryAwareArtifactDescriptor mockRepositoryAwareArtifactDescriptor;

    private RepositoryCache mockRepositoryCache;

    @Before
    public void setUp() {
        this.mockRepositoryAwareArtifactDescriptor = createMock(RepositoryAwareArtifactDescriptor.class);
        this.mockRepositoryCache = createMock(RepositoryCache.class);
        this.cacheingArtifactDescriptor = new CacheingArtifactDescriptor(this.mockRepositoryAwareArtifactDescriptor, this.mockRepositoryCache);
    }

    @Test
    public void testGetAttribute() {
        expect(this.mockRepositoryAwareArtifactDescriptor.getAttribute(isA(String.class))).andReturn(null);
        replay(this.mockRepositoryAwareArtifactDescriptor);
        this.cacheingArtifactDescriptor.getAttribute("");
        verify(this.mockRepositoryAwareArtifactDescriptor);
        
    }

    @Test
    public void testGetAttributes() {
        expect(this.mockRepositoryAwareArtifactDescriptor.getAttributes()).andReturn(null);
        replay(this.mockRepositoryAwareArtifactDescriptor);
        this.cacheingArtifactDescriptor.getAttributes();
        verify(this.mockRepositoryAwareArtifactDescriptor);
    }

    @Test
    public void testGetFilename() {
        expect(this.mockRepositoryAwareArtifactDescriptor.getFilename()).andReturn(null);
        replay(this.mockRepositoryAwareArtifactDescriptor);
        this.cacheingArtifactDescriptor.getFilename();
        verify(this.mockRepositoryAwareArtifactDescriptor);
    }

    @Test
    public void testGetName() {
        expect(this.mockRepositoryAwareArtifactDescriptor.getName()).andReturn(null);
        replay(this.mockRepositoryAwareArtifactDescriptor);
        this.cacheingArtifactDescriptor.getName();
        verify(this.mockRepositoryAwareArtifactDescriptor);
    }

    @Test
    public void testGetRepositoryName() {
        expect(this.mockRepositoryAwareArtifactDescriptor.getRepositoryName()).andReturn(null);
        replay(this.mockRepositoryAwareArtifactDescriptor);
        this.cacheingArtifactDescriptor.getRepositoryName();
        verify(this.mockRepositoryAwareArtifactDescriptor);
    }

    @Test
    public void testGetType() {
        expect(this.mockRepositoryAwareArtifactDescriptor.getType()).andReturn(null);
        replay(this.mockRepositoryAwareArtifactDescriptor);
        this.cacheingArtifactDescriptor.getType();
        verify(this.mockRepositoryAwareArtifactDescriptor);
    }

    @Test
    public void testGetUri() {
        expect(this.mockRepositoryCache.getUri(this.cacheingArtifactDescriptor)).andReturn(null);
        replay(this.mockRepositoryCache);
        this.cacheingArtifactDescriptor.getUri();
        verify(this.mockRepositoryCache);
    }
    
    @Test
    public void testGetRemoteUri() {
        expect(this.mockRepositoryAwareArtifactDescriptor.getUri()).andReturn(null);
        replay(this.mockRepositoryAwareArtifactDescriptor);
        this.cacheingArtifactDescriptor.getRemoteUri();
        verify(this.mockRepositoryAwareArtifactDescriptor);
    }

    /**
     * Test method for {@link org.eclipse.virgo.repository.internal.cacheing.CacheingArtifactDescriptor#getVersion()}.
     */
    @Test
    public void testGetVersion() {
        expect(this.mockRepositoryAwareArtifactDescriptor.getVersion()).andReturn(null);
        replay(this.mockRepositoryAwareArtifactDescriptor);
        this.cacheingArtifactDescriptor.getVersion();
        verify(this.mockRepositoryAwareArtifactDescriptor);
    }

}
