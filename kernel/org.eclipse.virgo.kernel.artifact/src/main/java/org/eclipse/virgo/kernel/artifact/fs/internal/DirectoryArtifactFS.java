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

package org.eclipse.virgo.kernel.artifact.fs.internal;

import java.io.File;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;


/**
 * An implementation of {@link ArtifactFS} that represents a directory on the filesystem
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class DirectoryArtifactFS implements ArtifactFS {

    private final File root;

    public DirectoryArtifactFS(File root) {
        if (root.exists() && !root.isDirectory()) {
            throw new IllegalArgumentException(String.format("File '%s' must be a directory", root.getAbsolutePath()));
        }
        this.root = root;
    }

    public ArtifactFSEntry getEntry(String name) {
        return new FileArtifactFSEntry(this.root, new File(root, name));
    }

    public File getFile() {
        return this.root;
    }

    @Override
    public String toString() {
        return this.root.getAbsolutePath();
    }

}
