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
        assertEquals("XX", manifest);
    }

    // Just in case manifests are somehow special cased in the implementation.
    @Test
    public void getNormalEntry() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
        InputStream inputStream = entry.getInputStream();
        String rawfile = new Scanner(inputStream).useDelimiter("\\A").next();
        assertEquals("rawfile", rawfile);
    }

    @Test
    public void getDirectoryEntry() {
        ArtifactFSEntry entry = this.artifactFS.getEntry("test");
        //XXX Not sure what should happen.
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
