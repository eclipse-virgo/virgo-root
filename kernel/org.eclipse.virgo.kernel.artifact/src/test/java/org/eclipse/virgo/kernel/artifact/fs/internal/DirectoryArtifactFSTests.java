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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.virgo.kernel.artifact.fs.internal.DirectoryArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.internal.FileArtifactFSEntry;
import org.junit.Test;


public class DirectoryArtifactFSTests {

    private final DirectoryArtifactFS artifactFS = new DirectoryArtifactFS(new File("build"));

    @Test
    public void getEntry() {
        assertTrue(this.artifactFS.getEntry("test") instanceof FileArtifactFSEntry);
    }
}
