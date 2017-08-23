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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import org.eclipse.virgo.repository.internal.cacheing.cache.descriptorhash.ArtifactDescriptorHash;
import org.eclipse.virgo.repository.util.FileDigest;
import org.eclipse.virgo.util.io.PathReference;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StandardSingleArtifactCache} encapsulates the cacheing of a specific artifact and its hash.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
class StandardSingleArtifactCache implements SingleArtifactCache {

    private static final String ALGORITHM_HASH_SEPARATOR = ":";

    private static final String HASH_SUFFIX = ".hash";

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardSingleArtifactCache.class);

    private final Object monitor = new Object();

    private final PathReference artifactCacheDirectory;

    private final URI repositoryArtifactURI;

    private final String fileName;

    private final PathReference artifactCacheFilePathReference;

    private final PathReference artifactHashFilePathReference;

    StandardSingleArtifactCache(String type, String name, Version version, URI repositoryArtifactURI, String fileName, File repositoryCacheDirectory) {
        this.artifactCacheDirectory = obtainArtifactCacheDirectory(type, name, version, repositoryCacheDirectory);
        this.repositoryArtifactURI = repositoryArtifactURI;
        this.fileName = fileName;
        this.artifactCacheFilePathReference = this.artifactCacheDirectory.newChild(this.fileName).createFile();
        this.artifactHashFilePathReference = this.artifactCacheDirectory.newChild(this.fileName + HASH_SUFFIX).createFile();
    }

    private static PathReference obtainArtifactCacheDirectory(String type, String name, Version version, File repositoryCacheDirectory) {
        PathReference repositoryDirectory = new PathReference(repositoryCacheDirectory);
        PathReference artifactCacheDirectory = repositoryDirectory.newChild(type).newChild(name).newChild(version.toString());
        artifactCacheDirectory.createDirectory();
        return artifactCacheDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public URI getCachedUri(ArtifactDescriptorHash artifactDescriptorHash) {
        synchronized (this.monitor) {
            String digestAlgorithm = artifactDescriptorHash.getDigestAlgorithm();
            String cachedHash = getHash(digestAlgorithm);
            if (cachedHash == null || !artifactDescriptorHash.matches(cachedHash)) {
                refresh(artifactDescriptorHash.getDigestAlgorithm());
                cachedHash = getHash(digestAlgorithm);
                if (cachedHash != null && artifactDescriptorHash.isPresent() && !artifactDescriptorHash.matches(cachedHash)) {
                    LOGGER.info("Newly cached artifact has a hash value different to that stored in the index");
                }
            }
            return getURI();
        }
    }

    private String getHash(String digestAlgorithm) {
        try {
            BufferedReader bufferedHashFileReader = new BufferedReader(getHashFileReader());
            try {
                String digestAlgorithmAndHash = bufferedHashFileReader.readLine();
                return extractHashForAlgorithm(digestAlgorithm, digestAlgorithmAndHash);
            } finally {
                bufferedHashFileReader.close();
            }
        } catch (IOException e) {
        }
        return null;
    }

    private String extractHashForAlgorithm(String digestAlgorithm, String digestAlgorithmAndHash) {
        String hash;
        hash = null;
        if (digestAlgorithmAndHash != null) {
            String[] s = digestAlgorithmAndHash.split(ALGORITHM_HASH_SEPARATOR);
            if (s.length == 2 && digestAlgorithm.equals(s[0])) {
                hash = s[1];
            }
        }
        return hash;
    }

    private void refresh(String digestAlgorithm) {
        /*
         * Note that the following method call is a foreign call to download the artifact inside a synchronized block on
         * this class's monitor. This breaks the policy of avoiding foreign calls, but is very unlikely to result in
         * deadlock because the download mechanism is a leaf of the kernel code and uses the JRE directly to perform
         * downloading. This is a deliberate trade off against the complexity of queuing up requests in a cancellable
         * way. The cancellable approach can be taken if and when downloading in the synchronized block becomes an issue
         * and a way of surfacing cancellation to the user is planned.
         */
        new Downloader(this.repositoryArtifactURI, this.artifactCacheFilePathReference).downloadArtifact();
        storeHash(digestAlgorithm);
    }

    private void storeHash(String digestAlgorithm) {
        try {
            String hash = FileDigest.getFileDigest(this.artifactCacheFilePathReference.toFile(), digestAlgorithm);
            Writer hashFileWriter = getHashFileWriter();
            try {
                hashFileWriter.write(digestAlgorithm + ALGORITHM_HASH_SEPARATOR + hash);
            } finally {
                hashFileWriter.close();
            }
        } catch (IOException e) {
            this.artifactHashFilePathReference.delete();
        } catch (NoSuchAlgorithmException e) {
            this.artifactHashFilePathReference.delete();
        }
    }

    private Writer getHashFileWriter() throws IOException {
        return new OutputStreamWriter(new FileOutputStream(this.artifactHashFilePathReference.toFile()), UTF_8);
    }

    private Reader getHashFileReader() throws IOException {
        return new InputStreamReader(new FileInputStream(this.artifactHashFilePathReference.toFile()), UTF_8);
    }

    private URI getURI() {
        return this.artifactCacheFilePathReference.exists() ? this.artifactCacheFilePathReference.toURI() : null;
    }

}
