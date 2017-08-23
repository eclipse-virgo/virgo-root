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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.not;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.SingleArtifactCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.StandardSingleArtifactCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.ArtifactDescriptorHash;
import org.eclipse.virgo.repository.util.FileDigest;
import org.eclipse.virgo.util.io.PathReference;

/**
 */
public class StandardArtifactCacheTests {

    private static final File REPOSITORY_CACHE_DIRECTORY = new File("build/cache");

    private static final File BUNDLE_DIRECTORY = new File("build/cacheing");

    private static final File BUNDLE_FILE = new File("build/cacheing/b.jar");

    private static final String ARTIFACT_TYPE = "bundle";

    private static final String ARTIFACT_NAME = "b";

    private static final Version ARTIFACT_VERSION = Version.parseVersion("3.2.1.ga");

    private URI artifactURI;

    private SingleArtifactCache artifactCache;

    private ArtifactDescriptorHash mockArtifactDescriptorHash;

    private PathReference b1;

    private PathReference b2;

    private String b2Hash;

    @Before
    public void setUp() throws Exception {
        PathReference bundleDir = new PathReference(new File("src/test/resources/cacheing"));
        b1 = bundleDir.newChild("b.jar");
        b2 = bundleDir.newChild("b2.jar");
        b2Hash = getArtifactHash(b2.toURI());
        BUNDLE_DIRECTORY.mkdir();
        replaceBundleFile(b1);

        this.artifactURI = BUNDLE_FILE.toURI();
        setUpCacheDirectory();
        this.artifactCache = new StandardSingleArtifactCache(ARTIFACT_TYPE, ARTIFACT_NAME, ARTIFACT_VERSION, this.artifactURI, BUNDLE_FILE.getName(),
            REPOSITORY_CACHE_DIRECTORY);
        this.mockArtifactDescriptorHash = createMock(ArtifactDescriptorHash.class);
    }

    private void replaceBundleFile(PathReference b) {
        deleteBundleFileIfExistent();
        b.copy(new PathReference(BUNDLE_FILE));
    }

    private void deleteBundleFileIfExistent() {
        if (BUNDLE_FILE.exists()) {
            boolean delete = BUNDLE_FILE.delete();
            assertTrue("Bundle file was not deleted", delete);
        }
    }

    private void setUpCacheDirectory() {
        PathReference cacheDir = new PathReference(REPOSITORY_CACHE_DIRECTORY);
        cacheDir.delete(true);
        cacheDir.createDirectory();
    }

    @After
    public void tearDown() throws Exception {
        verify(this.mockArtifactDescriptorHash);
        reset(this.mockArtifactDescriptorHash);
    }

    @Test
    public void testGetCachedUriNoHashInDescriptor() {
        expect(this.mockArtifactDescriptorHash.isPresent()).andReturn(false).anyTimes();
        expect(this.mockArtifactDescriptorHash.matches(isA(String.class))).andReturn(false).anyTimes();
        expect(this.mockArtifactDescriptorHash.getDigestAlgorithm()).andReturn(FileDigest.SHA_DIGEST_ALGORITHM).anyTimes();
        replay(this.mockArtifactDescriptorHash);
        URI cachedUri1 = this.artifactCache.getCachedUri(this.mockArtifactDescriptorHash);
        checkUriInCache(cachedUri1);
    }

    @Test
    public void testGetCachedUriRefreshNoHashInDescriptor() throws IOException {
        expect(this.mockArtifactDescriptorHash.isPresent()).andReturn(false).anyTimes();
        expect(this.mockArtifactDescriptorHash.matches(isA(String.class))).andReturn(false).anyTimes();
        expect(this.mockArtifactDescriptorHash.getDigestAlgorithm()).andReturn(FileDigest.SHA_DIGEST_ALGORITHM).anyTimes();
        replay(this.mockArtifactDescriptorHash);

        // Prime the cache.
        URI cachedUri1 = this.artifactCache.getCachedUri(this.mockArtifactDescriptorHash);
        checkUriInCache(cachedUri1);
        String hash1 = getArtifactHash(cachedUri1);

        // Get from the cache which causes the cache to be updated.
        replaceBundleFile(b2);
        URI cachedUri2 = this.artifactCache.getCachedUri(this.mockArtifactDescriptorHash);
        checkUriInCache(cachedUri2);
        String hash2 = getArtifactHash(cachedUri2);
        assertEquals(cachedUri1, cachedUri2);
        assertFalse((hash1.equals(hash2)));
    }

    @Test
    public void testGetCachedUriRefreshHashInDescriptor() throws IOException {
        expect(this.mockArtifactDescriptorHash.isPresent()).andReturn(true).anyTimes();
        expect(this.mockArtifactDescriptorHash.matches(not(eq(this.b2Hash)))).andReturn(false).anyTimes();
        expect(this.mockArtifactDescriptorHash.matches(eq(this.b2Hash))).andReturn(true).anyTimes();
        expect(this.mockArtifactDescriptorHash.getDigestAlgorithm()).andReturn(FileDigest.SHA_DIGEST_ALGORITHM).anyTimes();
        replay(this.mockArtifactDescriptorHash);

        // Prime the cache with the original b.jar. Its hash does not match that of the artifact descriptor.
        URI cachedUri1 = this.artifactCache.getCachedUri(this.mockArtifactDescriptorHash);
        checkUriInCache(cachedUri1);
        String hash1 = getArtifactHash(cachedUri1);

        // Get from the cache which causes the cache to be updated with the contents of b2.jar. Its hash matches that of
        // the artifact descriptor.
        replaceBundleFile(b2);
        URI cachedUri2 = this.artifactCache.getCachedUri(this.mockArtifactDescriptorHash);
        checkUriInCache(cachedUri2);
        String hash2 = getArtifactHash(cachedUri2);
        assertEquals(cachedUri1, cachedUri2);
        assertFalse((hash1.equals(hash2)));

        // Getting from the cache again will not update the cache.
        deleteBundleFileIfExistent();
        URI cachedUri3 = this.artifactCache.getCachedUri(this.mockArtifactDescriptorHash);
        checkUriInCache(cachedUri3);
        String hash3 = getArtifactHash(cachedUri3);
        assertEquals(cachedUri1, cachedUri3);
        assertEquals(hash2, hash3);
    }

    private void checkUriInCache(URI cachedUri) {
        URI cacheUri = REPOSITORY_CACHE_DIRECTORY.toURI();
        assertTrue(cachedUri.toString().startsWith(cacheUri.toString()));
    }

    private String getArtifactHash(URI cachedUri) throws IOException {
        File cachedArtifact = new File(cachedUri);
        return FileDigest.getFileShaDigest(cachedArtifact);
    }

}
