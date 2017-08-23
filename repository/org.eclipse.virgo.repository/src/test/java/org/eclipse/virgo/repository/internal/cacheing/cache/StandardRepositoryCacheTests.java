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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.StandardRepositoryCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.SingleArtifactCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.SingleArtifactCacheFactory;
import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.ArtifactDescriptorHash;
import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.ArtifactDescriptorHashFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;


/**
 */
public class StandardRepositoryCacheTests {

    private URI cachedURI;

    private SingleArtifactCache mockArtifactCache;

    private SingleArtifactCacheFactory mockArtifactCacheFactory;

    private static final String REPO_NAME = "SomeRepo";

    private static final String ARTIFACT_TYPE = "bundle";

    private static final String ARTIFACT_NAME = "b";

    private static final Version ARTIFACT_VERSION = Version.parseVersion("1.2");

    private URI remoteURI;

    private static final String ARTIFACT_FILE_NAME = "b";

    private RepositoryAwareArtifactDescriptor mockRepositoryAwareArtifactDescriptor;

    private ArtifactDescriptorHash mockArtifactDescriptorHash;

    private ArtifactDescriptorHashFactory mockArtifactDescriptorHashFactory;

    private RepositoryCache repositoryCache;

    @Before
    public void setUp() throws Exception {
        setUpArtifactCache();
        setUpArtifactDescriptor();
        setUpArtifactDescriptorHash();
        setUpRepositoryCache();
    }

    private void setUpArtifactCache() throws URISyntaxException {
        this.mockArtifactCache = createMock(SingleArtifactCache.class);

        this.cachedURI = new URI("file://cached");

        this.mockArtifactCacheFactory = createMock(SingleArtifactCacheFactory.class);
        expect(this.mockArtifactCacheFactory.getArtifactCache(isA(RepositoryAwareArtifactDescriptor.class), isA(File.class))).andReturn(
            this.mockArtifactCache).anyTimes();
    }

    private void setUpArtifactDescriptor() throws URISyntaxException {
        this.mockRepositoryAwareArtifactDescriptor = createMock(RepositoryAwareArtifactDescriptor.class);

        expect(this.mockRepositoryAwareArtifactDescriptor.getRepositoryName()).andReturn(REPO_NAME).anyTimes();

        expect(this.mockRepositoryAwareArtifactDescriptor.getType()).andReturn(ARTIFACT_TYPE).anyTimes();

        expect(this.mockRepositoryAwareArtifactDescriptor.getName()).andReturn(ARTIFACT_NAME).anyTimes();

        expect(this.mockRepositoryAwareArtifactDescriptor.getVersion()).andReturn(ARTIFACT_VERSION).anyTimes();

        expect(this.mockRepositoryAwareArtifactDescriptor.getFilename()).andReturn(ARTIFACT_FILE_NAME).anyTimes();

        this.remoteURI = new URI("http://remote");
        expect(this.mockRepositoryAwareArtifactDescriptor.getUri()).andReturn(this.remoteURI).anyTimes();
    }

    private void setUpArtifactDescriptorHash() {
        this.mockArtifactDescriptorHash = createMock(ArtifactDescriptorHash.class);

        this.mockArtifactDescriptorHashFactory = createMock(ArtifactDescriptorHashFactory.class);
        expect(this.mockArtifactDescriptorHashFactory.createArtifactDescriptorHash(this.mockRepositoryAwareArtifactDescriptor)).andReturn(
            this.mockArtifactDescriptorHash);
    }

    private void setUpRepositoryCache() {
        this.repositoryCache = new StandardRepositoryCache(REPO_NAME, new File("build"), this.mockArtifactCacheFactory,
            this.mockArtifactDescriptorHashFactory);
    }

    @After
    public void tearDown() throws Exception {
        verifyMocks();
        resetMocks();
    }

    @Test
    public void testGetUriNoHashInDescriptor() {
        expect(this.mockArtifactCache.getCachedUri(this.mockArtifactDescriptorHash)).andReturn(this.cachedURI);
        replayMocks();
        assertEquals(this.cachedURI, this.repositoryCache.getUri(this.mockRepositoryAwareArtifactDescriptor));
    }

    @Test
    public void testGetUriHashInDescriptorArtifactCached() {
        expect(this.mockArtifactCache.getCachedUri(this.mockArtifactDescriptorHash)).andReturn(this.cachedURI);
        replayMocks();
        assertEquals(this.cachedURI, this.repositoryCache.getUri(this.mockRepositoryAwareArtifactDescriptor));
    }

    private void replayMocks() {
        replay(this.mockArtifactCache, this.mockArtifactCacheFactory, this.mockArtifactDescriptorHashFactory, this.mockArtifactDescriptorHash,
            this.mockRepositoryAwareArtifactDescriptor);
    }

    private void verifyMocks() {
        verify(this.mockArtifactCache, this.mockArtifactCacheFactory, this.mockArtifactDescriptorHashFactory, this.mockArtifactDescriptorHash,
            this.mockRepositoryAwareArtifactDescriptor);
    }

    private void resetMocks() {
        reset(this.mockArtifactCache, this.mockArtifactCacheFactory, this.mockArtifactDescriptorHashFactory, this.mockArtifactDescriptorHash,
            this.mockRepositoryAwareArtifactDescriptor);
    }

}
