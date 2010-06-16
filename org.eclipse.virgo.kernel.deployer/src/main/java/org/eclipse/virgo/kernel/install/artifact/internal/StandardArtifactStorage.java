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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSFactory;
import org.eclipse.virgo.kernel.deployer.core.DeployerLogEvents;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.io.JarUtils;
import org.eclipse.virgo.util.io.PathReference;

final class StandardArtifactStorage implements ArtifactStorage {

    private static final List<String> JAR_EXTENSIONS = Arrays.asList("jar", "war", "par", "zip");

    private final PathReference sourcePathReference;

    private final PathReference baseStagingPathReference;

    private final PathReference pastStagingPathReference;

    private volatile ArtifactFS artifactFS;

    private final EventLogger eventLogger;

    private final Object monitor = new Object();

    public StandardArtifactStorage(PathReference sourcePathReference, PathReference baseStagingPathReference, ArtifactFSFactory artifactFSFactory,
        EventLogger eventLogger) {
        this.sourcePathReference = sourcePathReference;

        this.baseStagingPathReference = baseStagingPathReference;
        this.pastStagingPathReference = new PathReference(String.format("%s-past", this.baseStagingPathReference.getAbsolutePath()));

        this.eventLogger = eventLogger;

        synchronize();
        this.artifactFS = artifactFSFactory.create(this.baseStagingPathReference.toFile());
    }

    public void synchronize() {
        synchronize(this.sourcePathReference);
    }

    public ArtifactFS getArtifactFS() {
        return this.artifactFS;
    }

    public void synchronize(URI sourceUri) {
        synchronize(new PathReference(sourceUri));
    }

    public void rollBack() {
        synchronized (this.monitor) {
            unstashContent();
        }
    }

    public void delete() {
        this.baseStagingPathReference.delete(true);
    }

    private void synchronize(PathReference normalizedSourcePathReference) {
        synchronized (this.monitor) {
            stashContent();
            if (normalizedSourcePathReference != null && !normalizedSourcePathReference.isDirectory()
                && looksLikeAJar(normalizedSourcePathReference.getName())) {
                try {
                    JarUtils.unpackTo(normalizedSourcePathReference, this.baseStagingPathReference);
                } catch (IOException e) {
                    this.eventLogger.log(DeployerLogEvents.JAR_UNPACK_ERROR, e, normalizedSourcePathReference);
                    throw new RuntimeException(String.format("Exception unpacking '%s'", normalizedSourcePathReference), e);
                }
            } else if (normalizedSourcePathReference != null) {
                this.baseStagingPathReference.getParent().createDirectory();
                normalizedSourcePathReference.copy(this.baseStagingPathReference, true);
            } else {
                this.baseStagingPathReference.createDirectory();
            }
        }
    }

    private boolean looksLikeAJar(String name) {
        String fileName = name.toLowerCase(Locale.ENGLISH);

        int dotLocation = fileName.lastIndexOf('.');
        if (dotLocation == -1) {
            return false;
        }
        return JAR_EXTENSIONS.contains(fileName.substring(dotLocation + 1));
    }

    private void stashContent() {
        if (this.baseStagingPathReference.exists()) {
            this.pastStagingPathReference.delete(true);
            this.baseStagingPathReference.moveTo(this.pastStagingPathReference);
        }
    }

    private void unstashContent() {
        if (this.pastStagingPathReference.exists()) {
            this.baseStagingPathReference.delete(true);
            this.pastStagingPathReference.moveTo(this.baseStagingPathReference);
        }
    }

}
