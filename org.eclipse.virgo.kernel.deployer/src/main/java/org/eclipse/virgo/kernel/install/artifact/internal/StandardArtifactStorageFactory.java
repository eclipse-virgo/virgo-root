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

    public StandardArtifactStorageFactory(PathReference workDirectory, ArtifactFSFactory artifactFSFactory, EventLogger eventLogger) {
        this.workDirectory = workDirectory;
        this.artifactFSFactory = artifactFSFactory;
        this.eventLogger = eventLogger;
    }

    public ArtifactStorage create(File file, ArtifactIdentity artifactIdentity) {
        PathReference sourcePathReference = new PathReference(file);
        PathReference stagingPathReference = createStagingPathReference(artifactIdentity, file.getName());

        return new StandardArtifactStorage(sourcePathReference, stagingPathReference, this.artifactFSFactory, this.eventLogger);
    }

    public ArtifactStorage createDirectoryStorage(ArtifactIdentity artifactIdentity, String directoryName) {
        PathReference stagingPathReference = createStagingPathReference(artifactIdentity, directoryName);
        stagingPathReference.createDirectory();

        return new StandardArtifactStorage(null, stagingPathReference, this.artifactFSFactory, this.eventLogger);
    }

    private PathReference createStagingPathReference(ArtifactIdentity artifactIdentity, String name) {
        return this.workDirectory.newChild(DEPLOYER_STAGING_DIRECTORY).newChild(normalizeScopeName(artifactIdentity.getScopeName())).newChild(
            artifactIdentity.getType()).newChild(artifactIdentity.getName()).newChild(artifactIdentity.getVersion().toString()).newChild(name);
    }

    private String normalizeScopeName(String scopeName) {
        return scopeName == null ? "global" : scopeName;
    }

}
