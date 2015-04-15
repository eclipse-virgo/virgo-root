/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
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

import org.eclipse.virgo.util.io.PathReference;

/**
 * {@link GenerationalArtifactStore} implements {@link ArtifactStore} by creating a series of paths so that each version
 * of the artifact has a fresh path (unless the series wraps which is extremely unlikely in practice). This is designed
 * for storing a bundle which will be installed by reference into the OSGi framework since, when the bundle is updated,
 * the current version of the bundle may be in use until the bundle becomes unresolved.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * TODO Document concurrent semantics of GenerationalArtifactStore
 */
final class GenerationalArtifactStore extends AbstractArtifactStore implements ArtifactStore {

    private final PathReference baseDirectory;

    private long generation;

    private final String baseName;

    /**
     * Constructs a {@link GenerationalArtifactStore} in the directory of the specified path and prepares the path
     * storage ready for the new artifact. Each path produced by this store has the same filename as the specified path.
     */
    GenerationalArtifactStore(PathReference basePathReference) {
        super(basePathReference);
        this.baseDirectory = basePathReference.getParent();
        this.baseName = basePathReference.getName();

        final long recoveredGeneration = recoverLastGeneration(this.baseDirectory);
        this.generation = recoveredGeneration == -1 ? 0 : recoveredGeneration;

        PathReference currentPathReference = getGenerationPath(this.generation, this.baseDirectory, this.baseName);
        if (recoveredGeneration == -1) {
            currentPathReference.getParent().createDirectory();
            currentPathReference.delete(true);
        }
    }

    private static long recoverLastGeneration(PathReference baseDirectory) {
        long lastGeneration = -1;
        File file = baseDirectory.toFile();
        String[] children = file.list();
        if (children != null) {
            for (String child : children) {
                try {
                    long childGeneration = Long.parseLong(child);
                    if (childGeneration > lastGeneration) {
                        lastGeneration = childGeneration;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return lastGeneration;
    }

    /**
     * {@inheritDoc}
     */
    public PathReference getCurrentPath() {
        synchronized (this.monitor) {
            return getGenerationPath(this.generation);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        synchronized (this.monitor) {
            if (this.generation != 0) {
                getSavedPath().delete(true);
            }
            this.generation++;
            super.save();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore() {
        synchronized (this.monitor) {
            super.restore();
            this.generation--;
        }
    }

    protected PathReference getSavedPath() {
        return getGenerationPath(this.generation - 1);
    }

    private PathReference getGenerationPath(long generation) {
        return getGenerationPath(generation, this.baseDirectory, this.baseName);
    }

    private static PathReference getGenerationPath(long generation, PathReference baseDirectory, String baseName) {
        return baseDirectory.newChild(Long.toString(generation)).newChild(baseName);
    }

}
