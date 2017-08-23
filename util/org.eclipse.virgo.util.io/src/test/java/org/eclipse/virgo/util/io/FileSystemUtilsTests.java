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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.eclipse.virgo.util.io.FileSystemUtils;
import org.junit.Test;


/**
 */
public class FileSystemUtilsTests {

    @Test
    public void testDeleteRecursively() throws IOException {
        File f = new File("build/work");
        f.mkdir();
        assertTrue(f.exists());
        
        File e = new File(f.getAbsolutePath() + File.separator + "file.txt");
        e.createNewFile();
        assertTrue(e.exists());
        
        File m = new File(f.getAbsolutePath() + File.separator + "dir");
        m.mkdir();
        assertTrue(m.exists());
        
        assertTrue(FileSystemUtils.deleteRecursively(f));
        assertFalse(f.exists());
        
        assertTrue(FileSystemUtils.deleteRecursively("build/work"));
        assertFalse(f.exists());
    }
    
    @Test
    public void testCreateDirectoryIfNecessary() {
    	String path = FileSystemUtils.createDirectoryIfNecessary("build/work/test");

    	File f = new File(path);

    	assertTrue(f.exists());
        String path2 = FileSystemUtils.createDirectoryIfNecessary("build/work/test");
        assertEquals("Shouldn't have a different path if recreated unnecessarily", path, path2);
        
        assertTrue("File doesn't exist when created 'twice'.", f.exists());
        
        FileSystemUtils.deleteRecursively(f);
    }
    
    @Test
    public void testListFilesEmpty() {
        String path = FileSystemUtils.createDirectoryIfNecessary("build/work/testList");
        File f = new File(path);
        assertTrue(f.exists());
        
        String[] filenames = FileSystemUtils.list(f);
        assertTrue("list(f) returned null!", filenames!=null);
        assertTrue("Array of names of empty directory not zero length.", filenames.length==0);

        File[] files = FileSystemUtils.listFiles(f);
        assertTrue("listFiles(f) returned null!", files!=null);
        assertTrue("Array of files of empty directory not zero length.", files.length==0);
    }

    @Test
    public void testListFilesNonEmpty() throws IOException {
        String path = FileSystemUtils.createDirectoryIfNecessary("build/work/testList2");
        File f = new File(path);
        assertTrue(f.exists());
        
        new File(f.getAbsolutePath() + File.separator + "file1.txt").createNewFile();
        new File(f.getAbsolutePath() + File.separator + "file2.txt").createNewFile();
        
        String[] filenames = FileSystemUtils.list(f);
        assertTrue("list(f) returned null!", filenames!=null);
        assertTrue("Array of names of directory not length 2.", filenames.length==2);

        assertStringsInArray(filenames, "file1.txt", "file2.txt");
        
        String[] filteredFilenames = FileSystemUtils.list(f, new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return (name.equals("file1.txt"));
            }});
        assertTrue("list(f) returned null!", filteredFilenames!=null);
        assertTrue("Array of names of directory not length 1.", filteredFilenames.length==1);

        assertStringsInArray(filteredFilenames, "file1.txt");
        
        File[] files = FileSystemUtils.listFiles(f);
        assertTrue("listFiles(f) returned null!", files!=null);
        assertTrue("Array of files of directory not length 2.", files.length==2);

        assertFilesInArray(files, "file1.txt", "file2.txt");
        
        File[] filteredFiles = FileSystemUtils.listFiles(f, new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return (name.equals("file1.txt"));
            }});
        assertTrue("listFiles(f) returned null!", filteredFiles!=null);
        assertTrue("Array of files of directory not length 1.", filteredFiles.length==1);

        assertFilesInArray(filteredFiles, "file1.txt");
    }

    private static void assertStringsInArray(String[] filenames, String ... strings) {
        assertTrue("Strings " + String.valueOf(strings) + " not all found in array " + String.valueOf(filenames) + ".", allStringsInArray(filenames, strings));
    }

    private static boolean allStringsInArray(String[] stringArray, String[] strings) {
        for (String s : strings) {
            boolean found = false;
            for (String as : stringArray) {
                if (as.equals(s)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private static void assertFilesInArray(File[] files, String ... strings) {
        assertTrue("Strings " + String.valueOf(strings) + " not all found in array " + String.valueOf(files) + ".", allFilesInArray(files, strings));
    }

    private static boolean allFilesInArray(File[] fileArray, String[] strings) {
        for (String s : strings) {
            boolean found = false;
            for (File as : fileArray) {
                if (as.getName().equals(s)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Test
    public void testListFilesFiltered() throws IOException {
        String path = FileSystemUtils.createDirectoryIfNecessary("build/work/testList3");
        File f = new File(path);
        assertTrue(f.exists());
        
        new File(f.getAbsolutePath() + File.separator + "file1.txt").createNewFile();
        new File(f.getAbsolutePath() + File.separator + "file2.txt").createNewFile();
        
        File[] filteredFiles = FileSystemUtils.listFiles(f, new FileFilter(){
            public boolean accept(File pathname) {
                return (pathname.getName().equals("file2.txt"));
            }});
        assertTrue("listFiles(f) returned null!", filteredFiles!=null);
        assertTrue("Array of files of directory not length 1.", filteredFiles.length==1);

        assertFilesInArray(filteredFiles, "file2.txt");
    }

    @Test
    public void testListFilesNonExists() {
        String path = "build/work/testListNoDir";
        File f = new File(path);
        assertFalse(f.exists());
        
        try {
            FileSystemUtils.list(f);
            assertTrue("list(f) did not throw an exception!", false);
        } catch (FatalIOException ioe) {
            //ok
        }

        try {
            FileSystemUtils.listFiles(f);
            assertTrue("listFiles(f) did not throw an exception!", false);
        } catch (FatalIOException ioe) {
            //ok
        }
    }

}
