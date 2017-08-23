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

package org.eclipse.virgo.apps.repository.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.apps.repository.core.internal.ExportableIndexFilePool;
import org.eclipse.virgo.apps.repository.core.internal.FilePool;
import org.eclipse.virgo.apps.repository.core.internal.FilePoolException;
import org.eclipse.virgo.util.io.PathReference;



/**
 * Tests for {@link ExportableIndexFilePool} implementation of {@link FilePool}
 * 
 */
public class ExportableIndexFilePoolTests {

    private FilePool filePool;
    private File dirFile = new File("build/testfilepooldir");
    
    @Before
    public void setupFilePools() throws Exception {
        this.dirFile = this.dirFile.getCanonicalFile();
        clearTestDirs();
    }
    
    @After
    public void clearupFilePools() {
        clearTestDirs();
    }
    
    private void clearTestDirs() {
        PathReference pr = new PathReference(this.dirFile);
        pr.createDirectory().delete(true);
    }
    
    @Test 
    public void generateNextPoolFile() throws Exception {
        this.filePool = new ExportableIndexFilePool(this.dirFile, "testfilepoolfilename");  
        
        File file1 = this.filePool.generateNextPoolFile();
        assertNotNull(file1);
        assertEquals(new File(this.dirFile,"testfilepoolfilename.index0001"), file1);
        
        File file2 = this.filePool.generateNextPoolFile();
        assertNotNull(file2);
        assertEquals(new File(this.dirFile,"testfilepoolfilename.index0002"), file2);
        
        File nextFile = null;
        for (int i=0; i<10; ++i) {
            nextFile = this.filePool.generateNextPoolFile();
        }
        assertNotNull(nextFile);
        assertEquals(new File(this.dirFile,"testfilepoolfilename.index0012"), nextFile);
    }
    
    @Test(expected=FilePoolException.class) 
    public void getMostRecentPoolFileFromEmptyPool() throws Exception {
        this.filePool = new ExportableIndexFilePool(this.dirFile, "testfilepoolfilename");  

        this.filePool.getMostRecentPoolFile();
    }

    @Test 
    public void getMostRecentPoolFile() throws Exception {
        this.filePool = new ExportableIndexFilePool(this.dirFile, "testfilepoolfilename");  

        File[] files = new File[13];
        for (int i=0; i<13; ++i) {
            files[i] = this.filePool.generateNextPoolFile();
            this.filePool.putFileInPool(files[i]);
        }
        File testFile = this.filePool.getMostRecentPoolFile();
        assertNotNull(testFile);
        assertEquals(new File(this.dirFile,"testfilepoolfilename.index0013"), testFile);
        assertEquals("Directory holds wrong number of indexes", 10, this.dirFile.listFiles().length);
    }
    
    @Test
    public void initialiseWithDebris() throws Exception {
        new PathReference(new File(this.dirFile, "testfilepoolfilename.index9999")).createFile();
        
        this.filePool = new ExportableIndexFilePool(this.dirFile, "testfilepoolfilename");  
        
        File testFile = this.filePool.getMostRecentPoolFile();
        assertEquals("found old file", new File(this.dirFile,"testfilepoolfilename.index9999"), testFile);
        
        testFile = this.filePool.getMostRecentPoolFile();
        assertEquals("Get not repeatable", new File(this.dirFile,"testfilepoolfilename.index9999"), testFile);

        this.filePool.putFileInPool(this.filePool.generateNextPoolFile());
        
        testFile = this.filePool.getMostRecentPoolFile();
        assertEquals("Wraparound failed", new File(this.dirFile,"testfilepoolfilename.index0000"), testFile);

    }
    
}
