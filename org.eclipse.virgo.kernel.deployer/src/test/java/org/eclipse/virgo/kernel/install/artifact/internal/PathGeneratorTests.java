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
import org.junit.Test;

public class PathGeneratorTests {
    
    private static final String TEST_PATH = "target/pathGeneratorTests/";
    
    private static final String TEST_FILENAME = "some.jar";
    
    private PathGenerator artifactHistory;

    @Before
    public void setUp() throws Exception {
        this.artifactHistory = new PathGenerator(new PathReference(TEST_PATH + TEST_FILENAME));
    }

    @Test
    public void testGetCurrentPathReference() {
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
    }

    @Test
    public void testStash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.next();
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
        assertFalse(original.equals(c));
    }

    @Test
    public void testUnstash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.next();
        this.artifactHistory.previous();
        PathReference c = this.artifactHistory.getCurrentPath();
        checkPath(c);
        assertTrue(original.equals(c));

    }
    
    @Test
    public void testRepeatedStash() {
        PathReference original = this.artifactHistory.getCurrentPath();
        this.artifactHistory.next();
        PathReference next = this.artifactHistory.getCurrentPath();
        this.artifactHistory.next();
        PathReference last = this.artifactHistory.getCurrentPath();
        checkPath(last);
        assertFalse(original.equals(next));
        assertFalse(original.equals(last));
    }
    
    @Test
    public void testFileDeletionOnUnstash() {
        this.artifactHistory.next();
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createFile();
        assertTrue(c.exists());
        this.artifactHistory.previous();
        assertFalse(c.exists());
    }

    @Test
    public void testDirectoryDeletionOnUnstash() {
        this.artifactHistory.next();
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createDirectory();
        assertTrue(c.exists());
        this.artifactHistory.previous();
        assertFalse(c.exists());
    }

    @Test
    public void testFileDeletionOnDoubleStash() {
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createFile();
        assertTrue(c.exists());
        this.artifactHistory.next();
        assertTrue(c.exists());
        this.artifactHistory.next();
        assertFalse(c.exists());
    }
    
    @Test
    public void testDirectoryDeletionOnDoubleStash() {
        PathReference c = this.artifactHistory.getCurrentPath();
        c.createDirectory();
        assertTrue(c.exists());
        this.artifactHistory.next();
        assertTrue(c.exists());
        this.artifactHistory.next();
        assertFalse(c.exists());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testBadUnstash() {
        this.artifactHistory.previous();
    }

    @Test(expected=IllegalStateException.class)
    public void testDoubleUnstash() {
        this.artifactHistory.next();
        this.artifactHistory.next();
        this.artifactHistory.previous();
        this.artifactHistory.previous();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullConstructorPath() {
        new PathGenerator(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyConstructorPath() {
        new PathGenerator(new PathReference(""));
    }
    
    public void testDirectorylessConstructorPath() {
        PathGenerator ph = new PathGenerator(new PathReference("a"));
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

