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
import org.eclipse.virgo.util.common.StringUtils;

/**
 * An implementation of {@link ArtifactFS} that represents a single file. This implementation will only allow a call to
 * {@link #getEntry(String)} with a <code>null</code> value. A call with this argument returns an entry that matches
 * this single element.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public class FileArtifactFS implements ArtifactFS {

    private final File file;

    public FileArtifactFS(File file) {
        if (file.isDirectory()) {
            throw new IllegalArgumentException(String.format("This ArtifactFS cannot be created for '%s' as it is a directory",
                file.getAbsolutePath()));
        }
        this.file = file;
    }

    public ArtifactFSEntry getEntry(String name) {
        if (StringUtils.hasText(name)) {
            throw new IllegalArgumentException(
                "This ArtifactFS represents a single static file.  Requesting any entry other than null or \"\" is unsupported");
        }

        return new FileArtifactFSEntry(this.file.getParentFile(), this.file);
    }

    public final File getFile() {
        return this.file;
    }

    @Override
    public String toString() {
        return this.file.getAbsolutePath();
    }

}
