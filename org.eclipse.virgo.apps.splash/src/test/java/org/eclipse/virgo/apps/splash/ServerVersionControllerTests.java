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

package org.eclipse.virgo.apps.splash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.virgo.apps.splash.ServerVersionController;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 */
public class ServerVersionControllerTests {

    private static final String VERSION_PROPERTY = "virgo.server.version";
    
    private ServerVersionController serverVersionController = new ServerVersionController("target/lib/.version");
    
    @Test
    public void testGetServerVersionWithVersionFile() throws Exception{
        ModelAndView handleRequestInternal = this.serverVersionController.handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals("splash", handleRequestInternal.getViewName());
        assertTrue(handleRequestInternal.getModel().containsKey("version"));
        File versionFile = null;
        try {
            versionFile = writeVersionFile("test.version");
            this.serverVersionController = new ServerVersionController("target/lib/.version");
            handleRequestInternal = this.serverVersionController.handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse());
            String version = (String) handleRequestInternal.getModel().get("version");
            assertNotNull(version);
            assertEquals("test.version", version);
        } finally {
            if(versionFile != null && versionFile.exists()) {
                assertTrue(versionFile.delete());
            }
        }
    }
    
    @Test
    public void testGetServerVersionWithNoVersionFile() throws Exception{
        ModelAndView handleRequestInternal = this.serverVersionController.handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals("splash", handleRequestInternal.getViewName());
        assertTrue(handleRequestInternal.getModel().containsKey("version"));
        
        String version = (String) handleRequestInternal.getModel().get("version");
        assertNotNull(version);
        assertEquals("", version);
    }
    
    @Test
    public void testHandleRequestInternal() throws Exception{
        ModelAndView handleRequestInternal = this.serverVersionController.handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals("splash", handleRequestInternal.getViewName());
        assertTrue(handleRequestInternal.getModel().containsKey("version"));
    }
    
    private File writeVersionFile(String version) throws IOException{
        File libDir = new File("target/lib");
        libDir.mkdirs();
        File versionFile = new File(libDir, ".version");
        FileWriter fileWriter = new FileWriter(versionFile);
        fileWriter.write(VERSION_PROPERTY + "=" + version);
        fileWriter.close();
        return versionFile;
    }
	
}
