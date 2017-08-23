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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the FileCopyUtils class.
 *
 */
public class FileCopyUtilsTests {

    private final static File testDir = new File(new File("build"), "testFileCopyUtils");

    @Before
    @After
    public void resetTestDir() {
        FileSystemUtils.deleteRecursively(testDir);
        testDir.mkdirs();
    }

    @Test
    public void testCopyByteArrayToAndFromFile() throws Exception {
        byte[] content = "content".getBytes(UTF_8);
        File out = new File(testDir, "testByteArrayOut");
        FileCopyUtils.copy(content, out);

        assertTrue("File not the same length (" + content.length + ") as content (" + out.length() + ")", content.length == out.length());

        byte[] inBytes = FileCopyUtils.copyToByteArray(out);

        assertArrayEquals("Copy out and copy in not the same!", content, inBytes);
    }

    @Test
    public void testCopyFromInputStream() throws IOException {
        byte[] content = "content".getBytes(UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        int count = FileCopyUtils.copy(in, out);
        assertEquals(content.length, count);
        assertTrue(Arrays.equals(content, out.toByteArray()));
    }

    @Test
    public void testCopyFromByteArray() throws IOException {
        byte[] content = "content".getBytes(UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        FileCopyUtils.copy(content, out);
        assertTrue(Arrays.equals(content, out.toByteArray()));
    }

    @Test
    public void testCopyToByteArray() throws IOException {
        byte[] content = "content".getBytes(UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        byte[] result = FileCopyUtils.copyToByteArray(in);
        assertTrue(Arrays.equals(content, result));
    }

    @Test
    public void testCopyFromReader() throws IOException {
        String content = "content";
        StringReader in = new StringReader(content);
        StringWriter out = new StringWriter();
        int count = FileCopyUtils.copy(in, out);
        assertEquals(content.length(), count);
        assertEquals(content, out.toString());
    }

    @Test
    public void testCopyFromString() throws IOException {
        String content = "content";
        StringWriter out = new StringWriter();
        FileCopyUtils.copy(content, out);
        assertEquals(content, out.toString());
    }

    @Test
    public void testCopyToString() throws IOException {
        String content = "content";
        StringReader in = new StringReader(content);
        String result = FileCopyUtils.copyToString(in);
        assertEquals(content, result);
    }

}
