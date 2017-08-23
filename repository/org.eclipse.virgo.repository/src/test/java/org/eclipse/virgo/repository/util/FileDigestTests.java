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

package org.eclipse.virgo.repository.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.eclipse.virgo.repository.util.FileDigest;
import org.junit.Test;

/**
 */
public class FileDigestTests {
    
    private static final String SHA_HASH = "864adbb74bb984acf884f81a2a06f0b9e04d5231";
    
    private static final String MD5_HASH = "82a502028748fe243545e284bb0f5cc1";
    
    private static final File testFile = new File("src/test/resources/digest/test.jar");

    @Test
    public void testGetFileShaDigest() throws IOException {
        String hash = FileDigest.getFileShaDigest(testFile);
        assertEquals(SHA_HASH, hash);
    }

    @Test
    public void testGetFileDigest() throws NoSuchAlgorithmException, IOException {
        String hash = FileDigest.getFileDigest(testFile, FileDigest.MD5_DIGEST_ALGORITHM);
        assertEquals(MD5_HASH, hash);
    }
    
    @Test(expected=NoSuchAlgorithmException.class)
    public void testInvalidAlgorithm() throws NoSuchAlgorithmException, IOException {
        FileDigest.getFileDigest(testFile, "none");
    }

}
