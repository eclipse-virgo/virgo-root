/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.management.console;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UploadServletTests {

    @Test
    public void testDoPost() throws IOException {
        UploadServlet uploadServlet = new UploadServlet();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", null);
        uploadServlet.doPost(request, new MockHttpServletResponse());
    }

    @Test(expected = RuntimeException.class)
    public void testDoUploadFail() throws Exception {
        UploadServlet uploadServlet = new UploadServlet();
        FileItem fileItem = new DiskFileItem("foo", "json/application", false, "src/test/resources\\test.upload", 500, new File("build"));
        File stagingDir = new File("build");
        fileItem.getOutputStream();
        uploadServlet.doUpload(fileItem, stagingDir);
    }

    @Test
    public void testDoUpload() throws Exception {
        UploadServlet uploadServlet = new UploadServlet();
        FileItem fileItem = new DiskFileItem("foo", "json/application", false, "test.upload", 500, new File("build"));
        File stagingDir = new File("src/test/resources");
        fileItem.getOutputStream();
        File doUpload = uploadServlet.doUpload(fileItem, stagingDir);
        assertNotNull(doUpload);
        assertTrue("test.upload".equals(doUpload.getName()));
    }

    @Test
    public void testDoUploadWithFullPathUnix() throws Exception {
        UploadServlet uploadServlet = new UploadServlet();
        FileItem fileItem = new DiskFileItem("foo", "json/application", false, "src/test/resources/test.upload", 500, new File("build"));
        File stagingDir = new File("src/test/resources");
        fileItem.getOutputStream();
        File doUpload = uploadServlet.doUpload(fileItem, stagingDir);
        System.out.println(doUpload.getName());
        assertNotNull(doUpload);
        assertTrue("test.upload".equals(doUpload.getName()));
    }


    @Test
    public void testDoUploadWithFullPathWin() throws Exception {
        UploadServlet uploadServlet = new UploadServlet();
        FileItem fileItem = new DiskFileItem("foo", "json/application", false, "src\\test\\resources\\test.upload", 500, new File("build"));
        File stagingDir = new File("src/test/resources");
        fileItem.getOutputStream();
        File doUpload = uploadServlet.doUpload(fileItem, stagingDir);
        assertNotNull(doUpload);
        assertTrue("test.upload".equals(doUpload.getName()));
    }
}
