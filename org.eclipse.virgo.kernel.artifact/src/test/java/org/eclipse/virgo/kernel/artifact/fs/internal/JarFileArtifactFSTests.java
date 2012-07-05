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
import java.io.InputStream;
import java.util.Scanner;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
import org.junit.Test;

public class JarFileArtifactFSTests {

    private final FileArtifactFS artifactFS = new JarFileArtifactFS(new File("src/test/resources/artifacts/simple.jar"));

    @Test(expected = IllegalArgumentException.class)
    public void constructorDirectory() {
        new JarFileArtifactFS(new File("target"));
    }

    @Test
    public void getManifestEntry() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("META-INF/MANIFEST.MF");
        InputStream inputStream = entry.getInputStream();
        String manifest = new Scanner(inputStream).useDelimiter("\\A").next();
        assertEquals(
            "Manifest-Version: 1.0\nCreated-By: 1.6.0_07 (Apple Inc.)\nBundle-Name: test\nBundle-SymbolicName: test\nBundle-Version: 0.0.0\n\n",
            manifest);
        assertEquals("MANIFEST.MF", entry.getName());
        assertEquals("META-INF/MANIFEST.MF", entry.getPath());
    }

    // Just in case manifests are somehow special cased in the implementation.
    @Test
    public void getNormalEntry() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
        InputStream inputStream = entry.getInputStream();
        String rawfile = new Scanner(inputStream).useDelimiter("\\A").next();
        assertEquals("rawfile", rawfile);
        assertFalse(entry.isDirectory());
        assertEquals("rawfile", entry.getName());
        assertEquals("test/rawfile", entry.getPath());
    }

    @Test
    public void getDirectoryEntry() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/");
        assertTrue(entry.isDirectory());
        assertEquals("test", entry.getName());
        assertEquals("test/", entry.getPath());
    }

    @Test
    public void getDirectoryChildren() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(1, children.length);
        assertEquals("rawfile", children[0].getName());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void getFileChildren() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(0, children.length);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void getChildrenOfNonExistent() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("nosuch");
        ArtifactFSEntry[] children = entry.getChildren();
        assertEquals(0, children.length);
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
}
