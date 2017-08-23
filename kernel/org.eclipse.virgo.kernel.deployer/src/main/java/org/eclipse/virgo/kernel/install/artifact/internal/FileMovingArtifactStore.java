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

import org.eclipse.virgo.util.io.PathReference;

/**
 * {@link FileMovingArtifactStore} implements {@link ArtifactStore} and saves its artifact by moving it to another
 * location on disk.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class FileMovingArtifactStore extends AbstractArtifactStore implements ArtifactStore {

    private final PathReference currentPath;

    private final PathReference savedPath;

    /**
     * Constructs a {@link FileMovingArtifactStore} at the specified path and prepares the path storage ready for the
     * new artifact.
     */
    FileMovingArtifactStore(PathReference basePathReference) {
        super(basePathReference);
        this.currentPath = basePathReference;
        this.savedPath = new PathReference(String.format("%s-saved", this.currentPath.getAbsolutePath()));

        this.currentPath.getParent().createDirectory();
        this.currentPath.delete(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        if (this.currentPath.exists()) {
            this.savedPath.delete(true);
            this.currentPath.moveTo(this.savedPath);
        }
        super.save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore() {
        super.restore();
        if (this.savedPath.exists()) {
            this.savedPath.moveTo(this.currentPath);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathReference getCurrentPath() {
        return this.currentPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PathReference getSavedPath() {
        return this.savedPath;
    }

}
