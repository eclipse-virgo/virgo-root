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
import java.util.HashMap;
import java.util.Map;

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

    private static final String DEPLOYER_STAGING_DIRECTORY = "s";

    private final PathReference workDirectory;

    private final ArtifactFSFactory artifactFSFactory;

    private final EventLogger eventLogger;

    private final String unpackBundles;

    private final Object monitor = new Object();

    private final Map<PathReference, Long> uniqueId = new HashMap<PathReference, Long>();

    public StandardArtifactStorageFactory(PathReference workDirectory, ArtifactFSFactory artifactFSFactory, EventLogger eventLogger,
        String unpackBundles) {
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
        synchronized (this.monitor) {
            Long uniqueId = this.uniqueId.get(scopeDir);
            uniqueId = uniqueId == null ? 0L : uniqueId + 1;
            this.uniqueId.put(scopeDir, uniqueId);
            return scopeDir.newChild(uniqueId.toString());
        }
    }

}
