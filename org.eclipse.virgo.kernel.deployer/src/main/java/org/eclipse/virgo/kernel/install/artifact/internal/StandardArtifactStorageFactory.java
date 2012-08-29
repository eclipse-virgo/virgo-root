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

import java.io.File;


import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSFactory;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Standard implementation of {@link ArtifactStorage} that creates storage locations using a nested directory structure
 * of the form scope/type/name/version.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public final class StandardArtifactStorageFactory implements ArtifactStorageFactory {

    private static final String DEPLOYER_STAGING_DIRECTORY = "staging";

    private final PathReference workDirectory;

    private final ArtifactFSFactory artifactFSFactory;

    private final EventLogger eventLogger;
    
    private final String unpackBundles;

    public StandardArtifactStorageFactory(PathReference workDirectory, ArtifactFSFactory artifactFSFactory, EventLogger eventLogger, String unpackBundles) {
        this.workDirectory = workDirectory;
        this.artifactFSFactory = artifactFSFactory;
        this.eventLogger = eventLogger;
        this.unpackBundles = unpackBundles;
        this.workDirectory.newChild(DEPLOYER_STAGING_DIRECTORY).delete(true);
    }

    public ArtifactStorage create(File file, ArtifactIdentity artifactIdentity) {
        PathReference sourcePathReference = new PathReference(file);
        PathReference stagingPathReference = createStagingPathReference(artifactIdentity, file.getName());

        return new StandardArtifactStorage(sourcePathReference, stagingPathReference, this.artifactFSFactory, this.eventLogger, this.unpackBundles);
    }

    public ArtifactStorage createDirectoryStorage(ArtifactIdentity artifactIdentity, String directoryName) {
        PathReference stagingPathReference = createStagingPathReference(artifactIdentity, directoryName);
        stagingPathReference.createDirectory();

        return new StandardArtifactStorage(null, stagingPathReference, this.artifactFSFactory, this.eventLogger, this.unpackBundles);
    }

    private PathReference createStagingPathReference(ArtifactIdentity artifactIdentity, String name) {
        PathReference scopeDir = this.workDirectory.newChild(DEPLOYER_STAGING_DIRECTORY).newChild(normalizeScopeName(artifactIdentity.getScopeName()));
        return createNextChild(scopeDir).newChild(name);
    }

    private String normalizeScopeName(String scopeName) {
        return scopeName == null ? "global" : scopeName;
    }

    private PathReference createNextChild(PathReference scopeDir) {
        final long nextChildNumber = recoverLastChild(scopeDir) + 1;
        return scopeDir.newChild(Long.toString(nextChildNumber));
    }
    
    private static long recoverLastChild(PathReference scopeDir) {
        long lastChildNumber = -1;
        File file = scopeDir.toFile();
        String[] children = file.list();
        if (children != null) {
            for (String child : children) {
                try {
                    long childNumber = Long.parseLong(child);
                    if (childNumber > lastChildNumber) {
                        lastChildNumber = childNumber;
                    }
                } catch (NumberFormatException _) {
                }
            }
        }
        return lastChildNumber;
    }

}
