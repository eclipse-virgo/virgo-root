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

public class GenerationalArtifactStoreTests {
    
    private static final String TEST_PATH = "build/generationalArtifactStoreTests/";
    
    private static final String TEST_FILENAME = "some.jar";
    
    private ArtifactStore artifactStore;

    @Before
    public void setUp() throws Exception {
        (new PathReference(TEST_PATH)).delete(true);
        this.artifactStore = new GenerationalArtifactStore(new PathReference(TEST_PATH + TEST_FILENAME));
    }

    @Test
    public void testGetCurrentPathReference() {
        PathReference c = this.artifactStore.getCurrentPath();
        checkPath(c);
    }

    @Test
    public void testStash() {
        PathReference original = this.artifactStore.getCurrentPath();
        this.artifactStore.save();
        PathReference c = this.artifactStore.getCurrentPath();
        checkPath(c);
        assertFalse(original.equals(c));
    }

    @Test
    public void testUnstash() {
        PathReference original = this.artifactStore.getCurrentPath();
        this.artifactStore.save();
        this.artifactStore.restore();
        PathReference c = this.artifactStore.getCurrentPath();
        checkPath(c);
        assertTrue(original.equals(c));

    }
    
    @Test
    public void testRepeatedStash() {
        PathReference original = this.artifactStore.getCurrentPath();
        this.artifactStore.save();
        PathReference next = this.artifactStore.getCurrentPath();
        this.artifactStore.save();
        PathReference last = this.artifactStore.getCurrentPath();
        checkPath(last);
        assertFalse(original.equals(next));
        assertFalse(original.equals(last));
    }
    
    @Test
    public void testFileDeletionOnUnstash() {
        this.artifactStore.save();
        PathReference c = this.artifactStore.getCurrentPath();
        c.createFile();
        assertTrue(c.exists());
        this.artifactStore.restore();
        assertFalse(c.exists());
    }

    @Test
    public void testDirectoryDeletionOnUnstash() {
        this.artifactStore.save();
        PathReference c = this.artifactStore.getCurrentPath();
        c.createDirectory();
        assertTrue(c.exists());
        this.artifactStore.restore();
        assertFalse(c.exists());
    }

    @Test
    public void testFileDeletionOnDoubleStash() {
        PathReference c = this.artifactStore.getCurrentPath();
        c.createFile();
        assertTrue(c.exists());
        this.artifactStore.save();
        assertTrue(c.exists());
        this.artifactStore.save();
        assertFalse(c.exists());
    }
    
    @Test
    public void testDirectoryDeletionOnDoubleStash() {
        PathReference c = this.artifactStore.getCurrentPath();
        c.createDirectory();
        assertTrue(c.exists());
        this.artifactStore.save();
        assertTrue(c.exists());
        this.artifactStore.save();
        assertFalse(c.exists());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testBadUnstash() {
        this.artifactStore.restore();
    }

    @Test(expected=IllegalStateException.class)
    public void testDoubleUnstash() {
        this.artifactStore.save();
        this.artifactStore.save();
        this.artifactStore.restore();
        this.artifactStore.restore();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullConstructorPath() {
        new GenerationalArtifactStore(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEmptyConstructorPath() {
        new GenerationalArtifactStore(new PathReference(""));
    }
    
    @Test
    public void testFileRecovery() {
        PathReference p1 = this.artifactStore.getCurrentPath();
        p1.createFile();
        assertTrue(p1.exists());
        
        this.artifactStore.save();
        PathReference p2 = this.artifactStore.getCurrentPath();
        p2.createFile();
        assertTrue(p1.exists());
        assertTrue(p2.exists());
        
        this.artifactStore.save();
        PathReference p3 = this.artifactStore.getCurrentPath();
        p3.createFile();
        assertFalse(p1.exists());
        assertTrue(p2.exists());
        assertTrue(p3.exists());
        
        this.artifactStore.save();
        PathReference p4 = this.artifactStore.getCurrentPath();
        p4.createFile();
        assertFalse(p1.exists());
        assertFalse(p2.exists());
        assertTrue(p3.exists());
        assertTrue(p4.exists());
        
        ArtifactStore newArtifactStore = new GenerationalArtifactStore(new PathReference(TEST_PATH + TEST_FILENAME));
        PathReference p = newArtifactStore.getCurrentPath();
        assertEquals(p4, p);
        assertFalse(p1.exists());
        assertFalse(p2.exists());
        assertTrue(p3.exists());
        assertTrue(p4.exists());
    }
    
    @Test
    public void testDirectoryRecovery() {
        PathReference p1 = this.artifactStore.getCurrentPath();
        p1.createDirectory();
        assertTrue(p1.exists());
        
        this.artifactStore.save();
        PathReference p2 = this.artifactStore.getCurrentPath();
        p2.createDirectory();
        assertTrue(p1.exists());
        assertTrue(p2.exists());
        
        this.artifactStore.save();
        PathReference p3 = this.artifactStore.getCurrentPath();
        p3.createDirectory();
        assertFalse(p1.exists());
        assertTrue(p2.exists());
        assertTrue(p3.exists());
        
        this.artifactStore.save();
        PathReference p4 = this.artifactStore.getCurrentPath();
        p4.createDirectory();
        assertFalse(p1.exists());
        assertFalse(p2.exists());
        assertTrue(p3.exists());
        assertTrue(p4.exists());
        
        ArtifactStore newArtifactStore = new GenerationalArtifactStore(new PathReference(TEST_PATH + TEST_FILENAME));
        PathReference p = newArtifactStore.getCurrentPath();
        assertEquals(p4, p);
        assertFalse(p1.exists());
        assertFalse(p2.exists());
        assertTrue(p3.exists());
        assertTrue(p4.exists());
    }
       
    public void testDirectorylessConstructorPath() {
        ArtifactStore ph = new GenerationalArtifactStore(new PathReference("a"));
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

