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

import java.io.File;
import java.util.jar.JarFile;

import org.eclipse.virgo.kernel.artifact.fs.internal.FileArtifactFSEntry;
import org.junit.Test;

public class FileArtifactFSEntryTests {

    private final File file;

    public FileArtifactFSEntryTests() throws Exception {
        this.file = new File("src/test/resources/artifacts/exploded");
        new FileArtifactFSEntry(this.file, new File(this.file, JarFile.MANIFEST_NAME));
        new FileArtifactFSEntry(this.file, new File(this.file, "META-INF/"));
    }

    @Test
    public void getNameRoot() {
        assertEquals("", new FileArtifactFSEntry(this.file, this.file).getPath());
    }

    @Test
    public void getName() {
        assertEquals("test", new FileArtifactFSEntry(this.file, new File(this.file, "test")).getPath());
    }
}
