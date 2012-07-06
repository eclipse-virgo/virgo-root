/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    VMware Inc. - initial contribution
 */

package org.eclipse.virgo.kernel.artifact.fs.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;

/**
 * {@link JarFileArtifactFSEntry} is an {@link ArtifactFSEntry} implementation for JAR file entries.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe
 */
final class JarFileArtifactFSEntry implements ArtifactFSEntry {

    private final String entryName;

    private final JarFile jarFile;

    private final ZipEntry zipEntry;

    /**
     * Constructs a new {@link JarFileArtifactFSEntry} for the given file which is assumed to be in JAR format and the
     * given entry name.
     * 
     * @param file a JAR file
     * @param entryName the name of an entry
     * @throws IOException if the entry cannot be created
     */
    JarFileArtifactFSEntry(File file, String entryName) throws IOException {
        this(new JarFile(file), entryName);
    }

    private JarFileArtifactFSEntry(JarFile jarFile, String entryName) {
        this.entryName = entryName;
        this.jarFile = jarFile;
        this.zipEntry = jarFile.getEntry(entryName);
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        return this.entryName;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        String filePath = removeTrailingSlash();
        int lastDir = filePath.lastIndexOf("/");
        return filePath.substring(lastDir + 1);
    }

    private String removeTrailingSlash() {
        return this.entryName.endsWith("/") ? this.entryName.substring(0, this.entryName.length() - 1) : this.entryName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean delete() {
        throw new UnsupportedOperationException("This ArtifactFSEntry is a member of a JAR file. Deleting it is unsupported");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDirectory() {
        return exists() ? this.zipEntry.isDirectory() : false;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getInputStream() {
        if (!exists()) {
            throw new UnsupportedOperationException("Cannot open an input stream for a non-existent entry");
        }

        if (isDirectory()) {
            throw new UnsupportedOperationException("Cannot open an input stream for a directory");
        }
        
        try {
            return this.jarFile.getInputStream(this.zipEntry);
        } catch (IOException e) {
            // Preserve compatibility with current interface.
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException("This ArtifactFSEntry is a member of a JAR file. Writing it is unsupported");
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactFSEntry[] getChildren() {
        if (!isDirectory()) {
            throw new UnsupportedOperationException("Cannot get children of a non-directory entry");
        }
        Set<ArtifactFSEntry> children = new HashSet<ArtifactFSEntry>();
        if (exists()) {
            Enumeration<JarEntry> entries = this.jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String childEntry = entry.getName();
                if (childEntry.length() > this.entryName.length() && childEntry.startsWith(this.entryName)) {
                    children.add(new JarFileArtifactFSEntry(this.jarFile, childEntry));
                }
            }
        }
        return children.toArray(new ArtifactFSEntry[children.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactFS getArtifactFS() {
        throw new UnsupportedOperationException("getArtifactFS method not supported by JarFileArtifactFSEntry");
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists() {
        return hasEntry(this.jarFile, this.entryName);
    }

    private static boolean hasEntry(JarFile jarFile, String entryName) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String childEntry = entry.getName();
            if (entryName.equals(childEntry)) {
                return true;
            }
        }
        return false;
    }

}
