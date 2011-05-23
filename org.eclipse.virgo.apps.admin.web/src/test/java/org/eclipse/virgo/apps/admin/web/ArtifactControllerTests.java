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

package org.eclipse.virgo.apps.admin.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.eclipse.virgo.apps.admin.web.ArtifactController;
import org.eclipse.virgo.apps.admin.web.stubs.StubArtifactService;
import org.eclipse.virgo.apps.admin.web.stubs.StubDojoTreeFormatter;
import org.eclipse.virgo.apps.admin.web.stubs.StubRamAccessorHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.ModelAndView;

import org.eclipse.virgo.apps.admin.core.ArtifactService;

/**
 */
public class ArtifactControllerTests {
    
    private static final String TEST_PARENT = "foobar";
    
    private static final String TEST_TYPE = "bundle"; //To get a link to be generated
    
    private static final String TEST_NAME = "foo";
    
    private static final String TEST_VERSION = "bar";
    
    private final StubRamAccessorHelper stubRamAccessorHelper = new StubRamAccessorHelper();
    
	private ArtifactController artifactController;
	
	private ArtifactService stubApplicationManagerService = new StubArtifactService();

    private MockMultipartHttpServletRequest request;
    
    private MockHttpServletResponse responce;
    
	@Before
	public void setup() {
        this.artifactController = new ArtifactController(this.stubApplicationManagerService, new StubDojoTreeFormatter(), stubRamAccessorHelper);		
		this.request = new MockMultipartHttpServletRequest();
		this.responce = new MockHttpServletResponse();
	}
	
	@Test
	public void testListWithNoMessage() {
		ModelAndView mav = this.artifactController.overview(request);
		Map<String, Object> model = mav.getModel();
		assertFalse(model.containsKey("result"));
	}	
	
	@Test
	public void testListWithMessage() {
		this.request.addParameter("message", "TestMessage");
		ModelAndView mav = this.artifactController.overview(request);
		Map<String, Object> model = mav.getModel();
		assertEquals("TestMessage", model.get("result"));
	}

	@Test
	public void testDeploy() {
		ModelAndView mav = this.artifactController.deploy(request);
		Map<String, Object> model = mav.getModel();
		assertEquals("Error: Please select the artifact you would like to upload.", model.get("result"));
	}

    /**
     * @throws ServletRequestBindingException from actionStart()
     */
    @Test
    public void testStart() throws ServletRequestBindingException {
        this.request.addParameter("type", "dummyType");
        this.request.addParameter("name", "dummyName");
        this.request.addParameter("version", "dummyVersion");
        this.request.addParameter("region", "dummyRegion");
        ModelAndView mav = this.artifactController.actionStart(request);
        assertEquals("artifact-overview", mav.getViewName());
        Map<String, Object> model = mav.getModel();
        assertTrue(model.containsKey("result"));
        assertTrue("start".equals(model.get("result")));
        assertEquals("start", this.stubRamAccessorHelper.getLastMethodCalled());
    }

    /**
     * @throws ServletRequestBindingException from actionStop()
     */
    @Test
    public void testStop() throws ServletRequestBindingException {
        this.request.addParameter("type", "dummyType");
        this.request.addParameter("name", "dummyName");
        this.request.addParameter("version", "dummyVersion");
        this.request.addParameter("region", "dummyRegion");
        ModelAndView mav = this.artifactController.actionStop(request);
        assertEquals("artifact-overview", mav.getViewName());
        Map<String, Object> model = mav.getModel();
        assertTrue(model.containsKey("result"));
        assertTrue("stop".equals(model.get("result")));
        assertEquals("stop", this.stubRamAccessorHelper.getLastMethodCalled());
    }

    /**
     * @throws ServletRequestBindingException from actionRefresh()
     */
    @Test
    public void testRefresh() throws ServletRequestBindingException {
        this.request.addParameter("type", "dummyType");
        this.request.addParameter("name", "dummyName");
        this.request.addParameter("version", "dummyVersion");
        this.request.addParameter("region", "dummyRegion");
        ModelAndView mav = this.artifactController.actionRefresh(request);
        assertEquals("artifact-overview", mav.getViewName());
        Map<String, Object> model = mav.getModel();
        assertTrue(model.containsKey("result"));
        assertTrue("refresh".equals(model.get("result")));
        assertEquals("refresh", this.stubRamAccessorHelper.getLastMethodCalled());
    }

    /**
     * @throws ServletRequestBindingException from actionUninstall()
     */
    @Test
    public void testUninstall() throws ServletRequestBindingException {
        this.request.addParameter("type", "dummyType");
        this.request.addParameter("name", "dummyName");
        this.request.addParameter("version", "dummyVersion");
        this.request.addParameter("region", "dummyRegion");
        ModelAndView mav = this.artifactController.actionUninstall(request);
        assertEquals("artifact-overview", mav.getViewName());
        Map<String, Object> model = mav.getModel();
        assertTrue(model.containsKey("result"));
        assertTrue("uninstall".equals(model.get("result")));
        assertEquals("uninstall", this.stubRamAccessorHelper.getLastMethodCalled());
    }
    
    @Test
    public void testDataTypes() throws ServletRequestBindingException, IOException {
        this.artifactController.data(request, responce);
        assertEquals("application/json", responce.getContentType());
        assertEquals("getTypes", this.stubRamAccessorHelper.getLastMethodCalled());
        String contentAsString = responce.getContentAsString();
        assertTrue(contentAsString.contains("id"));
        assertTrue(contentAsString.contains("label"));
        assertTrue(contentAsString.contains("items"));
    }
    
    @Test
    public void testDataOfType() throws ServletRequestBindingException, IOException {
        this.request.addParameter("parent", TEST_PARENT);
        this.request.addParameter("type", TEST_TYPE);
        this.artifactController.data(request, responce);
        assertEquals("getArtifactsOfType", this.stubRamAccessorHelper.getLastMethodCalled());
        assertEquals("application/json", responce.getContentType());
        String contentAsString = responce.getContentAsString();
        assertTrue(contentAsString.contains("id"));
        assertTrue(contentAsString.contains("label"));
        assertTrue(contentAsString.contains("items"));
        assertTrue(contentAsString.contains(TEST_PARENT));
        assertTrue(contentAsString.contains(TEST_TYPE));
        assertTrue(contentAsString.contains(TEST_NAME));
        assertTrue(contentAsString.contains(TEST_VERSION));
    }
    
    @Test
    public void testDataOfSpecificArtifact() throws ServletRequestBindingException, IOException {
        this.request.addParameter("parent", "TEST_PARENT");
        this.request.addParameter("type", TEST_TYPE);
        this.request.addParameter("name", TEST_NAME);
        this.request.addParameter("version", TEST_VERSION);
        this.artifactController.data(request, responce);
        assertEquals("getArtifact", this.stubRamAccessorHelper.getLastMethodCalled());
        assertEquals("application/json", responce.getContentType());
        String contentAsString = responce.getContentAsString();
        assertTrue(contentAsString.contains("id"));
        assertTrue(contentAsString.contains("label"));
        assertTrue(contentAsString.contains("items"));
        assertTrue(contentAsString.contains(TEST_PARENT));
        assertTrue(contentAsString.contains(TEST_TYPE));
        assertTrue(contentAsString.contains(TEST_NAME));
        assertTrue(contentAsString.contains(TEST_VERSION));
    }
	
}
