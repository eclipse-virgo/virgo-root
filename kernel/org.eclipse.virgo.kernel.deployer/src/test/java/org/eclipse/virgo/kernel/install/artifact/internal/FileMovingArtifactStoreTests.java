/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.virgo.util.io.PathReference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FileMovingArtifactStoreTests {

    private static final String TEST_PATH = "build/fileMovingArtifactStoreTests/";

    private static final String TEST_FILENAME = "some.jar";

    private ArtifactStore artifactHistory;

    @Before
    public void setUp() throws Exception {
        PathReference basePathReference = new PathReference(TEST_PATH + TEST_FILENAME);
        basePathReference.delete(true);
        this.artifactHistory = new FileMovingArtifactStore(basePathReference);
    }

    @Test
    public void testGetCurrentPathReference() {
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
    }

    @Test
    public void testStash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.save();
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
        assertTrue(original.equals(c));
    }

    @Test
    public void testUnstash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.save();
        this.artifactHistory.restore();
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
        assertTrue(original.equals(c));
    }

    @Test
    public void testRepeatedStash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.save();
        PathReference next = this.artifactHistory.getCurrentPath();
        this.artifactHistory.save();
        PathReference last = this.artifactHistory.getCurrentPath();
        checkPath(last);
        assertTrue(original.equals(next));
        assertTrue(original.equals(last));
    }

    @Test
    public void testFileDeletionOnUnstash() {
        this.artifactHistory.save();
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createFile();
        assertTrue(c.exists());
        this.artifactHistory.restore();
        assertFalse(c.exists());
    }

    @Test
    public void testDirectoryDeletionOnUnstash() {
        this.artifactHistory.save();
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createDirectory();
        assertTrue(c.exists());
        this.artifactHistory.restore();
        assertFalse(c.exists());
    }

    @Test
    public void testFileDeletionOnDoubleStash() {
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createFile();
        assertTrue(c.exists());
        this.artifactHistory.save();
        assertFalse(c.exists());
        this.artifactHistory.save();
        assertFalse(c.exists());
    }

    @Test
    public void testDirectoryDeletionOnDoubleStash() {
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createDirectory();
        assertTrue(c.exists());
        this.artifactHistory.save();
        assertFalse(c.exists());
        this.artifactHistory.save();
        assertFalse(c.exists());
    }

    @Test(expected = IllegalStateException.class)
    public void testBadUnstash() {
        this.artifactHistory.restore();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleUnstash() {
        this.artifactHistory.save();
        this.artifactHistory.save();
        this.artifactHistory.restore();
        this.artifactHistory.restore();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullConstructorPath() {
        new FileMovingArtifactStore(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyConstructorPath() {
        new FileMovingArtifactStore(new PathReference(""));
    }

    @Test
    @Ignore("review the inactive / failing test")
    // TODO - review the (from the beginning) inactive test
    public void testDirectorylessConstructorPath() {
        ArtifactStore ph = new FileMovingArtifactStore(new PathReference("a"));
        assertEquals("a", ph.getCurrentPath().getName());
    }

    private void checkPath(PathReference c) {
        File file = c.toFile();
        assertTrue(file.toURI().toString().indexOf(TEST_PATH) != -1);
        assertEquals(TEST_FILENAME, c.getName());
        assertFalse(c.exists());
        PathReference parent = c.getParent();
        assertTrue(parent.isDirectory());
        assertTrue(parent.exists());
    }

}
