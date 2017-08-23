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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.DelegatingRepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.IdentityUriMapper;
import org.eclipse.virgo.repository.internal.StandardArtifactDescriptor;
import org.eclipse.virgo.repository.internal.StandardAttribute;
import org.eclipse.virgo.repository.internal.cacheing.cache.RepositoryCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.StandardRepositoryCache;
import org.eclipse.virgo.repository.internal.cacheing.cache.artifact.StandardSingleArtifactCacheFactory;
import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.StandardArtifactDescriptorHashFactory;
import org.eclipse.virgo.repository.util.FileDigest;
import org.eclipse.virgo.util.io.PathReference;

/**
 */
public class StandardRepositoryCacheIntegrationTests {

    private static final String HASH_ATTRIBUTE_NAME = "hash";

    private static final String HASH_ALGORITHM_ATTRIBUTE_NAME = "hash-algorithm";

    private static final String REPO_NAME = "repo";

    private static final File REPOSITORY_CACHE_DIRECTORY = new File("build/repocache");

    private static final File BUNDLE_DIRECTORY = new File("build/cacheing");

    private static final File BUNDLE_FILE = new File("build/cacheing/b.jar");

    private static final String ARTIFACT_TYPE = "bundle";

    private static final String ARTIFACT_NAME = "b";

    private static final Version ARTIFACT_VERSION = Version.parseVersion("3.2.1.ga");

    private URI artifactURI;

    private PathReference b1;

    private PathReference b2;

    private String b2Hash;

    private RepositoryCache repositoryCache;

    private RepositoryAwareArtifactDescriptor artifactDescriptor;

    @Before
    public void setUp() throws Exception {
        this.repositoryCache = new StandardRepositoryCache(REPO_NAME, REPOSITORY_CACHE_DIRECTORY, new StandardSingleArtifactCacheFactory(),
            new StandardArtifactDescriptorHashFactory());

        PathReference bundleDir = new PathReference(new File("src/test/resources/cacheing"));
        this.b1 = bundleDir.newChild("b.jar");
        this.b2 = bundleDir.newChild("b2.jar");
        this.b2Hash = getArtifactHash(this.b2.toURI());
        BUNDLE_DIRECTORY.mkdir();
        replaceBundleFile(this.b1);

        this.artifactURI = BUNDLE_FILE.toURI();
        setUpCacheDirectory();

    }

    private void setUpCacheDirectory() {
        PathReference cacheDir = new PathReference(REPOSITORY_CACHE_DIRECTORY);
        cacheDir.delete(true);
        cacheDir.createDirectory();
    }

    private void replaceBundleFile(PathReference b) {
        deleteBundleFile();
        b.copy(new PathReference(BUNDLE_FILE));
    }

    private void deleteBundleFile() {
        BUNDLE_FILE.delete();
    }

    @Test
    public void testGetCachedUriRefreshHashInDescriptor() throws IOException {
        Set<Attribute> attributes = new HashSet<Attribute>();
        attributes.add(new StandardAttribute(HASH_ALGORITHM_ATTRIBUTE_NAME, FileDigest.SHA_DIGEST_ALGORITHM));
        attributes.add(new StandardAttribute(HASH_ATTRIBUTE_NAME, this.b2Hash));
        ArtifactDescriptor delegate = new StandardArtifactDescriptor(this.artifactURI, ARTIFACT_TYPE, ARTIFACT_NAME, ARTIFACT_VERSION,
            BUNDLE_FILE.getName(), attributes);

        this.artifactDescriptor = new DelegatingRepositoryAwareArtifactDescriptor(delegate, REPO_NAME, new IdentityUriMapper());

        // Prime the cache with the original b.jar. Its hash does not match that of the artifact descriptor.
        URI cachedUri1 = this.repositoryCache.getUri(this.artifactDescriptor);
        checkUriInCache(cachedUri1);
        String hash1 = getArtifactHash(cachedUri1);

        // Get from the cache which causes the cache to be updated with the contents of b2.jar. Its hash matches that of
        // the artifact descriptor.
        replaceBundleFile(this.b2);
        URI cachedUri2 = this.repositoryCache.getUri(this.artifactDescriptor);
        checkUriInCache(cachedUri2);
        String hash2 = getArtifactHash(cachedUri2);
        assertEquals(cachedUri1, cachedUri2);
        assertFalse(hash1.equals(hash2));

        // Getting from the cache again will not update the cache.
        deleteBundleFile();
        URI cachedUri3 = this.repositoryCache.getUri(this.artifactDescriptor);
        checkUriInCache(cachedUri3);
        String hash3 = getArtifactHash(cachedUri3);
        assertEquals(cachedUri1, cachedUri3);
        assertEquals(hash2, hash3);
    }

    @Test
    public void testGetCachedUriRefreshNoHashInDescriptor() {
        Set<Attribute> attributes = new HashSet<Attribute>();
        ArtifactDescriptor delegate = new StandardArtifactDescriptor(this.artifactURI, ARTIFACT_TYPE, ARTIFACT_NAME, ARTIFACT_VERSION,
            BUNDLE_FILE.getName(), attributes);

        this.artifactDescriptor = new DelegatingRepositoryAwareArtifactDescriptor(delegate, REPO_NAME, new IdentityUriMapper());

        // Prime the cache with the original b.jar. Its hash does not match that of the artifact descriptor.
        URI cachedUri1 = this.repositoryCache.getUri(this.artifactDescriptor);
        checkUriInCache(cachedUri1);
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
