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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.virgo.util.io.FileSystemUtils;
import org.junit.Test;


/**
 */
public class FileSystemUtilsTests {

    @Test
    public void testDeleteRecursively() throws IOException {
        File f = new File("target/work");
        f.mkdir();
        assertTrue(f.exists());
        
        File e = new File(f.getAbsolutePath() + File.separator + "file.txt");
        e.createNewFile();
        assertTrue(e.exists());
        
        File m = new File(f.getAbsolutePath() + File.separator + "dir");
        m.mkdir();
        assertTrue(m.exists());
        
        FileSystemUtils.deleteRecursively(f);
        assertFalse(f.exists());
        
        assertFalse(FileSystemUtils.deleteRecursively("target/work"));
    }
    
    @Test
    public void testCreateDirectoryIfNecessary() {
    	String path = FileSystemUtils.createDirectoryIfNecessary("target/work/test");

    	File f = new File(path);
        assertTrue(f.exists());
        
        FileSystemUtils.deleteRecursively(f);
    }
}
