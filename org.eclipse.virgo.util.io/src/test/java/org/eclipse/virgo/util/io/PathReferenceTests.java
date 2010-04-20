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

package org.eclipse.virgo.util.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.virgo.util.io.FatalIOException;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.PathReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class PathReferenceTests {

    private static final String WORK_AREA_PATH = "target/work";

    private static final String TEST_FILE = "src/test/resources/test.txt";

    @Before public void workAreaSetUp() {
        File f = new File(WORK_AREA_PATH);
        if (f.exists()) {
            FileSystemUtils.deleteRecursively(f);
        }
        f.mkdir();
    }

    @After public void workAreaClean() {
        File f = new File(WORK_AREA_PATH);
        FileSystemUtils.deleteRecursively(f);
    }

    @Test public void createFromPath() {
        PathReference path = new PathReference("src/test/resources");
        assertNotNull(path.toFile());
    }

    @Test public void createFromFile() {
        PathReference path = new PathReference(new File("src/test/resources"));
        assertNotNull(path.toFile());
    }

    @Test public void exists() {
        assertTrue(new PathReference("src/test/resources").exists());
        assertFalse(new PathReference("src/test/resources/non-existent").exists());
    }

    @Test public void isFileOrDirectory() {
        assertTrue(new PathReference("src/test/resources").isDirectory());
        assertTrue(new PathReference("src/test/resources/test.txt").isFile());
    }

    @Test public void concat() {
        PathReference pr = PathReference.concat("src", "test", "resources", "test.txt");
        assertTrue(pr.exists());
        assertTrue(pr.isFile());
    }

    @Test public void create() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy.txt");
        assertFalse(pr.exists());
        pr.createFile();
        assertTrue(pr.exists());
        assertTrue(pr.isFile());
    }

    @Test public void createDirectory() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        assertFalse(pr.exists());
        pr.createDirectory();
        assertTrue(pr.exists());
        assertTrue(pr.isDirectory());
    }

    @Test public void createTree() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        assertFalse(pr.exists());
        PathReference file = pr.createDirectory().newChild("inner").createDirectory().newChild("foo.txt").createFile();
        assertTrue(file.exists());
    }

    @Test public void createTreeNested() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        assertFalse(pr.exists());
        PathReference ref = pr.newChild("inner").newChild("more").createDirectory();
        assertTrue(ref.exists());
    }

    @Test public void createTreeNestedWithFile() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        assertFalse(pr.exists());
        PathReference ref = pr.newChild("inner").newChild("more.txt").createFile();
        assertTrue(ref.exists());
    }

    @Test public void deleteNonRecursiveDir() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy_dir");
        PathReference nested = pr.createDirectory().newChild("foo").createDirectory().newChild("bar").createFile();
        assertTrue(pr.exists());
        assertTrue(nested.exists());
        pr.delete(false);
        assertTrue(pr.exists());
        assertTrue(nested.exists());
    }

    @Test public void deleteNonRecursiveFile() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy.txt");
        pr.createFile();
        assertTrue(pr.exists());
        pr.delete(false);
        assertFalse(pr.exists());
    }

    @Test public void deleteNonRecursiveEmptyDir() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        pr.createDirectory();
        assertTrue(pr.exists());
        pr.delete(false);
        assertFalse(pr.exists());
    }

    @Test public void deleteRecursive() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy_dir");
        PathReference nested = pr.createDirectory().newChild("foo").createDirectory().newChild("bar").createFile();
        assertTrue(pr.exists());
        assertTrue(nested.exists());
        pr.delete(true);
        assertFalse(pr.exists());
        assertFalse(nested.exists());
    }

    @Test public void copyFileToFile() {
        PathReference pr = new PathReference(TEST_FILE);
        assertTrue(pr.exists());

        PathReference dest = PathReference.concat(WORK_AREA_PATH, "out.txt");
        assertFalse(dest.exists());

        PathReference copy = pr.copy(dest, false);
        assertTrue(dest.exists());
        assertEquals(dest, copy);
    }

    @Test(expected = FatalIOException.class) public void copyFileToExistingFile() {
        PathReference pr = new PathReference(TEST_FILE);
        assertTrue(pr.exists());

        PathReference dest = PathReference.concat(WORK_AREA_PATH, "out.txt");
        dest.createFile();
        assertTrue(dest.exists());

        pr.copy(dest, false);
    }

    @Test public void copyFileToDirectory() {
        PathReference pr = new PathReference(TEST_FILE);
        assertTrue(pr.exists());

        PathReference dest = new PathReference(WORK_AREA_PATH);
        assertTrue(dest.exists());

        PathReference finalDest = dest.newChild(pr.getName());
        assertFalse(finalDest.exists());

        PathReference copy = pr.copy(dest, false);
        assertTrue(finalDest.exists());
        assertEquals(finalDest, copy);
    }

    @Test(expected = FatalIOException.class) public void copyFileToDirectoryWhenExists() {
        PathReference pr = new PathReference(TEST_FILE);
        assertTrue(pr.exists());

        PathReference dest = new PathReference(WORK_AREA_PATH);
        assertTrue(dest.exists());

        PathReference finalDest = dest.newChild(pr.getName());
        finalDest.createFile();
        assertTrue(finalDest.exists());

        pr.copy(dest, false);
    }

    @Test(expected = FatalIOException.class) public void copyDirNonEmptyNonRecursive() {
        PathReference pr = new PathReference(TEST_FILE).getParent();
        assertTrue(pr.exists());

        PathReference dest = new PathReference(WORK_AREA_PATH);
        assertTrue(dest.exists());

        pr.copy(dest, false);
    }

    @Test public void copyDirEmptyNonRecursive() {
        PathReference pr = new PathReference(WORK_AREA_PATH).newChild("src").createDirectory();
        assertTrue(pr.exists());

        PathReference dest = PathReference.concat(WORK_AREA_PATH, "dest");
        assertFalse(dest.exists());

        pr.copy(dest, false);

        assertTrue(dest.exists());
    }

    @Test public void copyDirNonEmptyRecursiveIntoExistingDir() {
        PathReference src = new PathReference(TEST_FILE).getParent();
        assertTrue(src.exists());

        PathReference dest = new PathReference(WORK_AREA_PATH);
        PathReference finalDest = dest.newChild(src.getName());
        assertFalse(finalDest.exists());

        src.copy(dest, true);

        assertTrue(finalDest.exists());
        assertTrue(finalDest.newChild("child").exists());
        assertTrue(finalDest.newChild("child").newChild("grand-child").exists());
        assertTrue(finalDest.newChild("child").newChild("grand-child").newChild("foo.txt").exists());
    }

    @Test public void copyDirNonEmptyRecursiveIntoNewDir() {
        PathReference src = new PathReference(TEST_FILE).getParent();
        assertTrue(src.exists());

        PathReference dest = PathReference.concat(WORK_AREA_PATH, "dest");
        assertFalse(dest.exists());

        src.copy(dest, true);

        assertTrue(dest.exists());
        assertTrue(dest.newChild("child").exists());
        assertTrue(dest.newChild("child").newChild("grand-child").exists());
        assertTrue(dest.newChild("child").newChild("grand-child").newChild("foo.txt").exists());
    }

    @Test public void copyDirNonEmptyRecursiveIntoNewDirWithFilter() {
        PathReference src = new PathReference(TEST_FILE).getParent();
        assertTrue(src.exists());

        PathReference dest = PathReference.concat(WORK_AREA_PATH, "dest");
        assertFalse(dest.exists());

        src.copy(dest, true, new PathReference.PathFilter() {

            public boolean matches(PathReference path) {
                return !"foo.txt".equals(path.getName());
            }

        });

        assertTrue(dest.exists());
        assertTrue(dest.newChild("child").exists());
        assertTrue(dest.newChild("child").newChild("grand-child").exists());
        assertFalse(dest.newChild("child").newChild("grand-child").newChild("foo.txt").exists());
    }
    
    @Test(expected = FileNotFoundException.class)
    public void testFileContentsFileDoesNotExist() throws IOException {
    	PathReference src = new PathReference(TEST_FILE + "2");
    	src.fileContents();
    }
    
    @Test
    public void testFileContents() throws IOException {
    	PathReference src = new PathReference(TEST_FILE);
    	assertEquals("hello", src.fileContents());
    }
}
