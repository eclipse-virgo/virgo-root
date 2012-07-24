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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.virgo.util.io.PathReference;
import org.junit.Before;
import org.junit.Test;

public class PathHistoryTests {
    
    private static final String TEST_PATH = "target/artifactHistoryTests/";
    
    private static final String TEST_FILENAME = "some.jar";
    
    private PathHistory artifactHistory;

    @Before
    public void setUp() throws Exception {
        this.artifactHistory = new PathHistory(new PathReference(TEST_PATH + TEST_FILENAME));
    }

    @Test
    public void testGetCurrentPathReference() {
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
    }

    @Test
    public void testStash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.stash();
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
        assertFalse(original.equals(c));
    }

    @Test
    public void testUnstash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.stash();
        this.artifactHistory.unstash();
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
        assertTrue(original.equals(c));

    }
    
    @Test
    public void testRepeatedStash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.stash();
        PathReference next = this.artifactHistory.getCurrentPath();
        this.artifactHistory.stash();
        PathReference last = this.artifactHistory.getCurrentPath();
        checkPath(last);
        assertFalse(original.equals(next));
        assertFalse(original.equals(last));
    }
    
    @Test
    public void testFileDeletionOnUnstash() {
        this.artifactHistory.stash();
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createFile();
        assertTrue(c.exists());
        this.artifactHistory.unstash();
        assertFalse(c.exists());
    }

    @Test
    public void testDirectoryDeletionOnUnstash() {
        this.artifactHistory.stash();
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createDirectory();
        assertTrue(c.exists());
        this.artifactHistory.unstash();
        assertFalse(c.exists());
    }

    @Test
    public void testFileDeletionOnDoubleStash() {
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createFile();
        assertTrue(c.exists());
        this.artifactHistory.stash();
        assertTrue(c.exists());
        this.artifactHistory.stash();
        assertFalse(c.exists());
    }
    
    @Test
    public void testDirectoryDeletionOnDoubleStash() {
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createDirectory();
        assertTrue(c.exists());
        this.artifactHistory.stash();
        assertTrue(c.exists());
        this.artifactHistory.stash();
        assertFalse(c.exists());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testBadUnstash() {
        this.artifactHistory.unstash();
    }

    @Test(expected=IllegalStateException.class)
    public void testDoubleUnstash() {
        this.artifactHistory.stash();
        this.artifactHistory.stash();
        this.artifactHistory.unstash();
        this.artifactHistory.unstash();
    }

    private void checkPath(PathReference c) {
        File file = c.toFile();
        assertTrue(file.toURI().toString().indexOf(TEST_PATH) != -1);
        //TODO: assertEquals(TEST_FILENAME, c.getName());
    }

}

