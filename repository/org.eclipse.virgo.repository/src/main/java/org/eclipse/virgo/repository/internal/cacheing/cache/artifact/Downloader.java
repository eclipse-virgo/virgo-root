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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.io.PathReference;

/**
 * {@link Downloader} is a utility used to download artifacts into an artifact cache.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class Downloader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    private final URI repositoryArtifactURI;

    private final PathReference artifactCacheFilePathReference;

    Downloader(URI sourceUri, PathReference destination) {
        this.repositoryArtifactURI = sourceUri;
        this.artifactCacheFilePathReference = destination;
    }

    /**
     * Downloads the artifact at the source URI to the destination PathReference. These parameters were supplied on the
     * constructor. If the artifact download is unsuccessful, deletes the destination file if it exists.
     */
    void downloadArtifact() {
        boolean downloadSuccessful = false;
        try {
            InputStream artifactInputStream = getArtifactInputStream();
            OutputStream artifactCacheOutputStream = getArtifactCacheOutputStream();
            if (artifactInputStream != null && artifactCacheOutputStream != null) {
                try {
                    FileCopyUtils.copy(artifactInputStream, artifactCacheOutputStream);
                    downloadSuccessful = true;
                } catch (IOException e) {
                    LOGGER.error("Error downloading artifact", e);
                    throw new RuntimeException("Error downloading artifact '" + this.repositoryArtifactURI +"'", e);
                }
            } else if (artifactInputStream != null) {
            	try {
					artifactInputStream.close();
				} catch (IOException e) {
					LOGGER.error("Error closing artifactInputStream", e);
				}
            } else if (artifactCacheOutputStream != null) {
            	try {
					artifactCacheOutputStream.close();
				} catch (IOException e) {
					LOGGER.error("Error closing artifactCacheOutputStream", e);
				}
            }
        } finally {
            if (!downloadSuccessful) {
                LOGGER.warn("Artifact download failed. Cleaning up target file {}.", this.artifactCacheFilePathReference.toAbsoluteReference());
                this.artifactCacheFilePathReference.delete();
            }
        }
    }

    private InputStream getArtifactInputStream() {
        try {
            URL url = this.repositoryArtifactURI.toURL();
            URLConnection urlConnection = url.openConnection();
            return urlConnection.getInputStream();
        } catch (MalformedURLException e) {
            LOGGER.error("Error accessing repository artifact", e);
        } catch (IOException e) {
            LOGGER.error("Error accessing repository artifact", e);
        }
        return null;
    }

    private OutputStream getArtifactCacheOutputStream() {
        try {
            this.artifactCacheFilePathReference.delete();
            return new FileOutputStream(this.artifactCacheFilePathReference.toFile());
        } catch (FileNotFoundException e) {
            LOGGER.error("Error accessing repository artifact download destination file", e);
        }
        return null;
    }
}
