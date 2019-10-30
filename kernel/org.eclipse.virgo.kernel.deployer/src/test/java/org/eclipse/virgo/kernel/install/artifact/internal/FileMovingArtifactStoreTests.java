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

import org.eclipse.virgo.util.io.PathReference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class FileMovingArtifactStoreTests {

    @Rule
    public TemporaryFolder artifactStore = new TemporaryFolder();

    private static final String TEST_FILENAME = "some.jar";

    private ArtifactStore artifactHistory;

    @Before
    public void setUp() {
        Path artifactStoreRoot = Paths.get(artifactStore.getRoot().toURI());
        PathReference basePathReference = new PathReference(artifactStoreRoot + File.separator + TEST_FILENAME);
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
        assertEquals(original, c);
    }

    @Test
    public void testUnstash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.save();
        this.artifactHistory.restore();
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
        assertEquals(original, c);
    }

    @Test
    public void testRepeatedStash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.save();
        PathReference next = this.artifactHistory.getCurrentPath();
        this.artifactHistory.save();
        PathReference last = this.artifactHistory.getCurrentPath();
        checkPath(last);
        assertEquals(original, next);
        assertEquals(original, last);
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
        assertThat(file.toURI().toString(), containsString(artifactStore.getRoot().toString()));
        assertEquals(TEST_FILENAME, c.getName());
        assertFalse("PathReference " + c + " should not exist", c.exists());
        PathReference parent = c.getParent();
        assertNotNull(parent);
        assertTrue(parent.isDirectory());
        assertTrue(parent.exists());
    }

}
