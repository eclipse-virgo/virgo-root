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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;

/**
 * {@link JarFileArtifactFSEntry} is an {@link ArtifactFSEntry} implementation for JAR file entries.
 * <p/>
 * The implementation uses ZipInputStream specifically to avoid JarFile's caching behaviour, inherited from that of
 * ZipFile. See the note on caching in http://java.sun.com/developer/technicalArticles/Programming/compression/
 * JarFile's caching behaviour is unsuitable as it produces incorrect results when a JAR file is replaced with a new
 * version since the cache returns entries from the old version.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe
 */
final class JarFileArtifactFSEntry implements ArtifactFSEntry {

    private final File file;

    private final String entryName;

    /**
     * Constructs a new {@link JarFileArtifactFSEntry} for the given file which is assumed to be in JAR format and the
     * given entry name.
     * 
     * @param file a JAR file
     * @param entryName the name of an entry
     * @throws IOException if the entry cannot be created
     */
    JarFileArtifactFSEntry(File file, String entryName) {
        this.file = file;
        this.entryName = entryName;
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
        ZipEntry zipEntry = findZipEntry();
        return zipEntry != null ? zipEntry.isDirectory() : false;
    }

    private ZipEntry findZipEntry() {
        JarFileScanner scanner = new JarFileScanner();
        try {
            ZipEntry entry = scanner.getNextEntry();
            while (entry != null) {
                if (this.entryName.equals(entry.getName())) {
                    return entry;
                }
                entry = scanner.getNextEntry();
            }
            return null;
        } finally {
            scanner.close();
        }
    }

    /**
     * {@inheritDoc}
     */
	public InputStream getInputStream() {
        JarFileScanner scanner = new JarFileScanner();
        ZipEntry entry = scanner.getNextEntry();
        while (entry != null && !this.entryName.equals(entry.getName())) {
            entry = scanner.getNextEntry();
        }

        if (entry == null) {
            scanner.close();
            throw new UnsupportedOperationException("Cannot open an input stream for a non-existent entry");
        }

        if (entry.isDirectory()) {
            scanner.close();
            throw new UnsupportedOperationException("Cannot open an input stream for a directory");
        }
        return scanner.getZipInputStream();
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
            JarFileScanner scanner = new JarFileScanner();
            try {
                ZipEntry entry = scanner.getNextEntry();
                while (entry != null) {
                    String childEntry = entry.getName();
                    if (childEntry.length() > this.entryName.length() && childEntry.startsWith(this.entryName)) {
                        children.add(new JarFileArtifactFSEntry(this.file, childEntry));
                    }
                    entry = scanner.getNextEntry();
                }
            } finally {
                scanner.close();
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
        return findZipEntry() != null;
    }

    private class JarFileScanner implements Closeable {

        private final ZipInputStream zipInputStream;

        public JarFileScanner() {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(file));
            } catch (FileNotFoundException _) {
            }
            this.zipInputStream = is == null ? null : new ZipInputStream(is);
        }

        public ZipEntry getNextEntry() {
            if (this.zipInputStream != null) {
                try {
                    return this.zipInputStream.getNextEntry();
                } catch (IOException _) {
                }
            }
            return null;
        }

        public ZipInputStream getZipInputStream() {
            return this.zipInputStream;
        }

        public void close() {
            if (this.zipInputStream != null) {
                try {
                    this.zipInputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
