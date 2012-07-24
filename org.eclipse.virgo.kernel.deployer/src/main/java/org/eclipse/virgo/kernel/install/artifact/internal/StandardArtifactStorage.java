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
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.io.JarUtils;
import org.eclipse.virgo.util.io.PathReference;

final class StandardArtifactStorage implements ArtifactStorage {

    private static final String DEPLOYER_UNPACK_BUNDLES_TRUE = "true";

    private static final List<String> ALWAYS_UNPACKED_EXTENSIONS = Arrays.asList("par", "zip");

    private static final List<String> CONFIGURABLY_UNPACKED_EXTENSIONS = Arrays.asList("jar", "war");

    private final PathReference sourcePathReference;

    private final ArtifactFSFactory artifactFSFactory;

    private final EventLogger eventLogger;

    private final boolean unpackBundles;
    
    private final PathHistory artifactHistory;

    public StandardArtifactStorage(PathReference sourcePathReference, PathReference baseStagingPathReference, ArtifactFSFactory artifactFSFactory,
        EventLogger eventLogger, String unpackBundlesOption) {
        this.sourcePathReference = sourcePathReference;

        this.artifactFSFactory = artifactFSFactory;

        this.eventLogger = eventLogger;

        this.unpackBundles = (unpackBundlesOption == null) || DEPLOYER_UNPACK_BUNDLES_TRUE.equalsIgnoreCase(unpackBundlesOption);
        
        artifactHistory = new PathHistory(baseStagingPathReference);

        synchronize(this.sourcePathReference, false);
    }


    public void synchronize() {
        synchronize(this.sourcePathReference, true);
    }

    public ArtifactFS getArtifactFS() {
        return this.artifactFSFactory.create(this.artifactHistory.getCurrentPath().toFile());
    }

    public void synchronize(URI sourceUri) {
        synchronize(new PathReference(sourceUri), true);
    }

    public void rollBack() {
        this.artifactHistory.unstash();
    }

    public void delete() {
        PathReference currentPathReference = this.artifactHistory.getCurrentPath();
        currentPathReference.delete(true);
    }

    private void synchronize(PathReference normalizedSourcePathReference, boolean stash) {
        PathReference currentPathReference;
        if (stash) {
            this.artifactHistory.stash();
            currentPathReference = this.artifactHistory.getCurrentPath();
        } else {
            currentPathReference = this.artifactHistory.getCurrentPath();
            currentPathReference.delete(true);
        }

        if (normalizedSourcePathReference != null && !normalizedSourcePathReference.isDirectory()
            && needsUnpacking(normalizedSourcePathReference.getName())) {
            try {
                JarUtils.unpackTo(normalizedSourcePathReference, currentPathReference);
            } catch (IOException e) {
                this.eventLogger.log(DeployerLogEvents.JAR_UNPACK_ERROR, e, normalizedSourcePathReference);
                throw new RuntimeException(String.format("Exception unpacking '%s'", normalizedSourcePathReference), e);
            }
        } else if (normalizedSourcePathReference != null) {
            currentPathReference.getParent().createDirectory();
            normalizedSourcePathReference.copy(currentPathReference, true);
        } else {
            currentPathReference.createDirectory();
        }
    }

    private boolean needsUnpacking(String name) {
        String fileName = name.toLowerCase(Locale.ENGLISH);

        int dotLocation = fileName.lastIndexOf('.');
        if (dotLocation == -1) {
            return false;
        }
        String fileExtension = fileName.substring(dotLocation + 1);
        // Always unpack .par/.zip. Unpack .jar/.war if and only if kernel property deployer.unpackBundles is either not
        // specified or is "true"
        return ALWAYS_UNPACKED_EXTENSIONS.contains(fileExtension) || (this.unpackBundles && CONFIGURABLY_UNPACKED_EXTENSIONS.contains(fileExtension));
    }

}
