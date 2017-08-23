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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSFactory;
import org.eclipse.virgo.kernel.artifact.fs.StandardArtifactFSFactory;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.PathReference;

final class FileArtifactFSEntry implements ArtifactFSEntry {

    private final ArtifactFSFactory artifactFSFactory = new StandardArtifactFSFactory();

    private final File root;

    private final File file;

    public FileArtifactFSEntry(File root, File file) {
        this.root = root;
        this.file = file;
    }

    public boolean delete() {
        return new PathReference(this.file).delete(true);
    }

    public ArtifactFSEntry[] getChildren() {
        List<ArtifactFSEntry> children = new ArrayList<ArtifactFSEntry>();
        if (this.file.isDirectory()) {
            for (File child : FileSystemUtils.listFiles(this.file)) {
                children.add(new FileArtifactFSEntry(this.root, child));
            }
        }
        return children.toArray(new ArtifactFSEntry[children.size()]);
    }

    public InputStream getInputStream() {
        if (!this.file.exists()) {
            throw new UnsupportedOperationException(String.format("Cannot open an input stream for '%s' as it does not exist",
                this.file.getAbsolutePath()));
        }

        if (this.file.isDirectory()) {
            throw new UnsupportedOperationException(String.format("Cannot open an input stream for '%s' as it is a directory",
                this.file.getAbsolutePath()));
        }

        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(String.format("Unable to open an input stream for '%s'", this.file.getAbsolutePath()), e);
        }
    }

    public String getPath() {
        String relativePath = this.file.getAbsolutePath().substring(this.root.getAbsolutePath().length());
        if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            return relativePath.substring(1);
        }
        return relativePath;

    }

    public String getName() {
        return this.file.getName();
    }

    public OutputStream getOutputStream() {
        if (this.file.exists()) {
            if (this.file.isDirectory()) {
                throw new UnsupportedOperationException(String.format("Cannot open an ouput stream for '%s' as it is a directory",
                    this.file.getAbsolutePath()));
            }
        } else {
            File parentDir = this.file.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IllegalStateException("Failed to create directory " + parentDir + " prior to creating stream for " + this.file);
            }
        }

        try {
            return new FileOutputStream(this.file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(String.format("Unable to open an output stream for '%s'", this.file.getAbsolutePath()), e);
        }
    }

    public boolean isDirectory() {
        return this.file.isDirectory();
    }

    public String toString() {
        return this.file.getAbsolutePath();
    }

    public ArtifactFS getArtifactFS() {
        return this.artifactFSFactory.create(this.file);
    }

    public boolean exists() {
        return this.file.exists();
    }

}
