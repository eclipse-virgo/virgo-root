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
import static org.junit.Assert.assertNull;
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

    private static final String WORK_AREA_PATH = "build/work";
    
    private static final String WORK_AREA2_PATH = "build/work2";

    private static final String TEST_FILE = "src/test/resources/test.txt";

    private static final String TEST_FILE2 = "test2.txt";

    @Before 
    public void workAreaSetUp() throws Exception{
        resetWorkArea(new File(WORK_AREA_PATH));
        
        File f2 = new File(WORK_AREA2_PATH);
        resetWorkArea(f2);
        File testFile = new File(f2, TEST_FILE2);
        testFile.createNewFile();
    }

    private void resetWorkArea(File f) {
        if (f.exists()) {
            FileSystemUtils.deleteRecursively(f);
        }
        f.mkdir();
    }

    @After 
    public void workAreaClean() {
        FileSystemUtils.deleteRecursively(new File(WORK_AREA_PATH));
        FileSystemUtils.deleteRecursively(new File(WORK_AREA2_PATH));
    }

    @Test 
    public void createFromPath() {
        PathReference path = new PathReference("src/test/resources");
        assertNotNull(path.toFile());
    }

    @Test 
    public void createFromFile() {
        PathReference path = new PathReference(new File("src/test/resources"));
        assertNotNull(path.toFile());
    }

    @Test 
    public void exists() {
        assertTrue(new PathReference("src/test/resources").exists());
        assertFalse(new PathReference("src/test/resources/non-existent").exists());
    }

    @Test 
    public void isFileOrDirectory() {
        assertTrue(new PathReference("src/test/resources").isDirectory());
        assertTrue(new PathReference("src/test/resources/test.txt").isFile());
    }

    @Test 
    public void concat() {
        PathReference pr = PathReference.concat("src", "test", "resources", "test.txt");
        assertTrue(pr.exists());
        assertTrue(pr.isFile());
    }

    @Test 
    public void create() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy.txt");
        assertFalse(pr.exists());
        pr.createFile();
        assertTrue(pr.exists());
        assertTrue(pr.isFile());
    }

    @Test 
    public void createDirectory() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        assertFalse(pr.exists());
        pr.createDirectory();
        assertTrue(pr.exists());
        assertTrue(pr.isDirectory());
    }

    @Test 
    public void createTree() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        assertFalse(pr.exists());
        PathReference file = pr.createDirectory().newChild("inner").createDirectory().newChild("foo.txt").createFile();
        assertTrue(file.exists());
    }

    @Test 
    public void createTreeNested() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        assertFalse(pr.exists());
        PathReference ref = pr.newChild("inner").newChild("more").createDirectory();
        assertTrue(ref.exists());
    }

    @Test 
    public void createTreeNestedWithFile() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        assertFalse(pr.exists());
        PathReference ref = pr.newChild("inner").newChild("more.txt").createFile();
        assertTrue(ref.exists());
    }

    @Test 
    public void deleteNonRecursiveDir() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy_dir");
        PathReference nested = pr.createDirectory().newChild("foo").createDirectory().newChild("bar").createFile();
        assertTrue(pr.exists());
        assertTrue(nested.exists());
        pr.delete(false);
        assertTrue(pr.exists());
        assertTrue(nested.exists());
    }

    @Test 
    public void deleteNonRecursiveFile() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy.txt");
        pr.createFile();
        assertTrue(pr.exists());
        pr.delete(false);
        assertFalse(pr.exists());
    }

    @Test 
    public void deleteNonRecursiveEmptyDir() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy");
        pr.createDirectory();
        assertTrue(pr.exists());
        pr.delete(false);
        assertFalse(pr.exists());
    }

    @Test 
    public void deleteRecursive() {
        PathReference pr = PathReference.concat(WORK_AREA_PATH, "dummy_dir");
        PathReference nested = pr.createDirectory().newChild("foo").createDirectory().newChild("bar").createFile();
        assertTrue(pr.exists());
        assertTrue(nested.exists());
        pr.delete(true);
        assertFalse(pr.exists());
        assertFalse(nested.exists());
    }

    @Test 
    public void copyFileToFile() {
        PathReference pr = new PathReference(TEST_FILE);
        assertTrue(pr.exists());

        PathReference dest = PathReference.concat(WORK_AREA_PATH, "out.txt");
        assertFalse(dest.exists());

        PathReference copy = pr.copy(dest, false);
        assertTrue(dest.exists());
        assertEquals(dest, copy);
    }
    
    @Test 
    public void moveFileToFile() {
        PathReference pr = PathReference.concat(WORK_AREA2_PATH, TEST_FILE2);
        assertTrue(pr.exists());
        
        PathReference dest = PathReference.concat(WORK_AREA2_PATH, "out.txt");
        assertFalse(dest.exists());

        PathReference moved = pr.moveTo(dest);
        assertEquals("Resulting reference not destination.", dest, moved);
        assertTrue("File " + dest + " doesn't exist -- file not moved.", dest.exists());
        assertFalse("File " + pr + " still exists -- not moved.", pr.exists());
    }

    @Test 
    public void moveDirToDir() {
        PathReference src = new PathReference(TEST_FILE).getParent();
        assertTrue(src.exists());
        
        PathReference srcCopy = PathReference.concat(WORK_AREA2_PATH, "moveFromSrc");
        src.copy(srcCopy, true);
        assertTrue(srcCopy.exists());

        PathReference dest = PathReference.concat(WORK_AREA2_PATH, "moveToDest");
        assertFalse(dest.exists());

        srcCopy.moveTo(dest);

        assertTrue(dest + " does not exist", dest.exists());
        assertTrue("child in destination does not exist", dest.newChild("child").exists());
        assertTrue("child/grand-child in destination does not exist", dest.newChild("child").newChild("grand-child").exists());
        assertTrue("child/grand-child/foo.txt in destination does not exist", dest.newChild("child").newChild("grand-child").newChild("foo.txt").exists());
        
        assertFalse(srcCopy + " still exists", srcCopy.exists());
        assertFalse("child in source still exists", srcCopy.newChild("child").exists());
        assertFalse("child/grand-child in source still exists", srcCopy.newChild("child").newChild("grand-child").exists());
        assertFalse("child/grand-child/foo.txt in source still exists", srcCopy.newChild("child").newChild("grand-child").newChild("foo.txt").exists());
    }
    
    @Test 
    public void moveToFails() {
        PathReference pr = PathReference.concat(WORK_AREA2_PATH, TEST_FILE2);
        assertTrue(pr.exists());
        
        PathReference dest = PathReference.concat(WORK_AREA2_PATH, "out.txt");
        assertFalse(dest.exists());

        pr.copy(dest);
        assertTrue("File " + dest + " doesn't exist -- file not copied.", dest.exists());
        
        PathReference moved = null;
        try {
            moved = pr.moveTo(dest);
        } catch (FatalIOException fioe) {
            fioe.printStackTrace();
        }
        assertNull("Expecting moved to be null", moved);
        assertTrue("File " + pr + " should still exist.", pr.exists());
        assertTrue("File " + dest + " doesn't exist!", dest.exists());
    }
    
    @Test
    public void moveDirToDirFast() {
        PathReference src = new PathReference(TEST_FILE).getParent();
        assertTrue(src.exists());
        
        PathReference srcCopy = PathReference.concat(WORK_AREA2_PATH, "moveFromSrc");
        src.copy(srcCopy, true);
        assertTrue(srcCopy.exists());

        PathReference dest = PathReference.concat(WORK_AREA2_PATH, "moveToDest");
        assertFalse(dest.exists());

        boolean movedFast = false;
        try {
            srcCopy.fastMoveTo(dest);
            movedFast = true;
        } catch (FatalIOException fioe) {
            fioe.printStackTrace();
            movedFast = false;
        }

        assertTrue("Fast move failed!", movedFast);
        
        assertTrue(dest + " does not exist", dest.exists());
        assertTrue("child in destination does not exist", dest.newChild("child").exists());
        assertTrue("child/grand-child in destination does not exist", dest.newChild("child").newChild("grand-child").exists());
        assertTrue("child/grand-child/foo.txt in destination does not exist", dest.newChild("child").newChild("grand-child").newChild("foo.txt").exists());
        
        assertFalse(srcCopy + " still exists", srcCopy.exists());
        assertFalse("child in source still exists", srcCopy.newChild("child").exists());
        assertFalse("child/grand-child in source still exists", srcCopy.newChild("child").newChild("grand-child").exists());
        assertFalse("child/grand-child/foo.txt in source still exists", srcCopy.newChild("child").newChild("grand-child").newChild("foo.txt").exists());
    }

    @Test(expected = FatalIOException.class) 
    public void copyFileToExistingFile() {
        PathReference pr = new PathReference(TEST_FILE);
        assertTrue(pr.exists());

        PathReference dest = PathReference.concat(WORK_AREA_PATH, "out.txt");
        dest.createFile();
        assertTrue(dest.exists());

        pr.copy(dest, false);
    }

    @Test 
    public void copyFileToDirectory() {
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

    @Test(expected = FatalIOException.class) 
    public void copyFileToDirectoryWhenExists() {
        PathReference pr = new PathReference(TEST_FILE);
        assertTrue(pr.exists());

        PathReference dest = new PathReference(WORK_AREA_PATH);
        assertTrue(dest.exists());

        PathReference finalDest = dest.newChild(pr.getName());
        finalDest.createFile();
        assertTrue(finalDest.exists());

        pr.copy(dest, false);
    }

    @Test(expected = FatalIOException.class) 
    public void copyDirNonEmptyNonRecursive() {
        PathReference pr = new PathReference(TEST_FILE).getParent();
        assertTrue(pr.exists());

        PathReference dest = new PathReference(WORK_AREA_PATH);
        assertTrue(dest.exists());

        pr.copy(dest, false);
    }

    @Test 
    public void copyDirEmptyNonRecursive() {
        PathReference pr = new PathReference(WORK_AREA_PATH).newChild("src").createDirectory();
        assertTrue(pr.exists());

        PathReference dest = PathReference.concat(WORK_AREA_PATH, "dest");
        assertFalse(dest.exists());

        pr.copy(dest, false);

        assertTrue(dest.exists());
    }

    @Test 
    public void copyDirNonEmptyRecursiveIntoExistingDir() {
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

    @Test 
    public void copyDirNonEmptyRecursiveIntoNewDir() {
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

    @Test 
    public void copyDirNonEmptyRecursiveIntoNewDirWithFilter() {
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
    
    @Test
    public void testTouch() throws Exception {
        File file = new File(new File(WORK_AREA2_PATH), TEST_FILE2);
        PathReference src = new PathReference(file);
        long lm = file.lastModified();
        Thread.sleep(1000L); //allow present time to change a bit
        assertTrue(src.touch());
        long newlm = file.lastModified();
        assertTrue("LastModified ("+newlm+") not any greater now ("+lm+").", lm < newlm);
    }
    
    @Test
    public void testHashCode() {
        PathReference src = new PathReference(TEST_FILE);
        assertTrue("Hash code is zero!", 0 != src.hashCode());
    }

    @Test
    public void testGetAbsolutePath() throws Exception {
        PathReference src = new PathReference(TEST_FILE);
        assertEquals("Absolute path not the same as File()", new File(TEST_FILE).getAbsolutePath(), src.toAbsoluteReference().getAbsolutePath());
    }   
    @Test
    public void testGetCanonicalPath() throws Exception {
        PathReference src = new PathReference(TEST_FILE);
        assertEquals("Absolute path not the same as File()", new File(TEST_FILE).getCanonicalPath(), src.toCanonicalReference().getCanonicalPath());
    }
    
}
