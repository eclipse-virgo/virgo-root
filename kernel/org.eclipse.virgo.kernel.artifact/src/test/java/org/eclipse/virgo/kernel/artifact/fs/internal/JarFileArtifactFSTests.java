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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.junit.Test;

public class JarFileArtifactFSTests {

    private final FileArtifactFS artifactFS = new JarFileArtifactFS(new File("src/test/resources/artifacts/simple.jar"));

    private final FileArtifactFS artifactFSWithMissingEntries = new JarFileArtifactFS(new File(
        "src/test/resources/artifacts/bundle-with-missing-entries.jar"));

    @Test(expected = IllegalArgumentException.class)
    public void constructorDirectory() {
        new JarFileArtifactFS(new File("build"));
    }

    /**
     * This usage is not expected, but is tested to ensure predictable behaviour.
     */
    @Test
    public void constructorNonJarFile() {
        ArtifactFS fileArtifactFS = new JarFileArtifactFS(new File("src/test/resources/properties/foo.properties"));
        File file = fileArtifactFS.getFile();
        assertEquals("foo.properties", file.getName());
        ArtifactFSEntry entry = fileArtifactFS.getEntry("");
        assertTrue(entry instanceof FileArtifactFSEntry);
        assertFalse(entry instanceof JarFileArtifactFSEntry);
        ArtifactFSEntry badEntry = fileArtifactFS.getEntry("blah");
        assertFalse(badEntry.exists());
    }

    /**
     * Just in case manifests are somehow special cased in the implementation.
     */
    @Test
    public void getManifestEntry() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("META-INF/MANIFEST.MF");
        InputStream inputStream = entry.getInputStream();
        try (Scanner scanner = new Scanner(inputStream)) {
            String manifest = scanner.useDelimiter("\\A").next();
            assertEquals(
                "Manifest-Version: 1.0\nCreated-By: 1.6.0_07 (Apple Inc.)\nBundle-Name: test\nBundle-SymbolicName: test\nBundle-Version: 0.0.0\n\n",
                manifest);
        }
        assertEquals("MANIFEST.MF", entry.getName());
        assertEquals("META-INF/MANIFEST.MF", entry.getPath());
    }

    @Test
    public void getNormalEntry() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
        InputStream inputStream = entry.getInputStream();
        try (Scanner scanner = new Scanner(inputStream)) {
            String rawfile = scanner.useDelimiter("\\A").next();
            assertEquals("rawfile", rawfile);
        }
        assertFalse(entry.isDirectory());
        assertEquals("rawfile", entry.getName());
        assertEquals("test/rawfile", entry.getPath());
        assertTrue(entry.exists());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getOutputStream() throws IOException {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
        try (OutputStream dummy = entry.getOutputStream()) {
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void delete() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
        entry.delete();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getEntryArtifactFS() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
        entry.getArtifactFS();
    }

    @Test
    public void getDirectoryEntry() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/");
        assertTrue(entry.isDirectory());
        assertEquals("test", entry.getName());
        assertEquals("test/", entry.getPath());
        assertTrue(entry.exists());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDirectoryInputStream() throws IOException {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/");
        try (InputStream dummy = entry.getInputStream()) {
        }
    }

    @Test
    public void getDirectoryChildren() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(1, children.length);
        assertEquals("rawfile", children[0].getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getFileChildren() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(0, children.length);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getChildrenOfNonExistentFile() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(0, children.length);
    }

    @Test
    public void getPathOfNonExistentFile() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
        assertEquals("x/nosuch", entry.getPath());
    }

    @Test
    public void getNameOfNonExistentFile() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
        assertEquals("nosuch", entry.getName());
    }

    @Test
    public void isDirectoryOfNonExistentFile() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
        assertFalse(entry.isDirectory());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getInputStreamOfNonExistentFile() throws IOException {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
        try (InputStream dummy = entry.getInputStream()) {
        }
    }

    @Test
    public void getExistenceOfNonExistentFile() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
        assertFalse(entry.exists());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getChildrenOfNonExistentDirectory() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(0, children.length);
    }

    @Test
    public void getPathOfNonExistentDirectory() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
        assertEquals("x/nosuch/", entry.getPath());
    }

    @Test
    public void getNameOfNonExistentDirectory() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
        assertEquals("nosuch", entry.getName());
    }

    @Test
    public void isDirectoryOfNonExistentDirectory() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
        assertFalse(entry.isDirectory());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getInputStreamOfNonExistentDirectory() throws IOException {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
        try (InputStream dummy = entry.getInputStream()) {
        }
    }

    @Test
    public void getExistenceOfNonExistentDirectory() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
        assertFalse(entry.exists());
    }

    @Test
    public void getEntryNull() {
        assertTrue(this.artifactFS.getEntry(null) instanceof FileArtifactFSEntry);
    }

    @Test
    public void getFile() {
        File file = this.artifactFS.getFile();
        assertEquals("simple.jar", file.getName());
    }

    @Test
    public void testDirectory() {
        ArtifactFS artifactFS = new JarFileArtifactFS(new File("src/test/resources/artifacts/bundle.jar"));
        ArtifactFSEntry dir = artifactFS.getEntry("META-INF/spring/");
        assertTrue(dir.exists());
        ArtifactFSEntry[] files = dir.getChildren();
        assertEquals(1, files.length);
        ArtifactFSEntry entry = files[0];
        InputStream inputStream = entry.getInputStream();
        try (Scanner scanner = new Scanner(inputStream)) {
            String contents = scanner.useDelimiter("\\A").next();
            assertTrue(contents.startsWith("<beans xmlns=\"http://www.springframework.org/schema/beans\""));
        }
    }

    @Test
    public void getMissingDirectoryEntry() {
        ArtifactFSEntry entry = this.artifactFSWithMissingEntries.getEntry("META-INF/spring/");
        assertTrue(entry.exists());
        assertTrue(entry.isDirectory());
    }

    @Test
    public void getMissingDirectoryEntryName() {
        ArtifactFSEntry entry = this.artifactFSWithMissingEntries.getEntry("META-INF/spring/");
        assertEquals("spring", entry.getName());
        assertEquals("META-INF/spring/", entry.getPath());
    }

    @Test
    public void getMissingDirectoryEntryChildren() {
        ArtifactFSEntry entry = this.artifactFSWithMissingEntries.getEntry("META-INF/spring/");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(1, children.length);
    }

    @Test
    public void getChildrenIncludingMissingEntry() {
        ArtifactFSEntry entry = this.artifactFSWithMissingEntries.getEntry("META-INF/");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(3, children.length);
        boolean found = false;
        for (ArtifactFSEntry artifactFSEntry : children) {
            if (artifactFSEntry.getPath().equals("META-INF/spring/")) {
                found = true;
            }
        }
        assertTrue(found);
    }

}
