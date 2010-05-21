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

import java.util.Map;

import org.eclipse.virgo.apps.admin.web.StateController;
import org.eclipse.virgo.apps.admin.web.internal.DumpListFormatterUtil;
import org.eclipse.virgo.apps.admin.web.stubs.StubDumpListFormatterUtil;
import org.eclipse.virgo.apps.admin.web.stubs.StubStateHolder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.ModelAndView;

import org.eclipse.virgo.apps.admin.core.StateHolder;


/**
 */
public class StateControllerTests {
    
    private MockHttpServletRequest request;
    
    private StateController stateController;
    
    private StateHolder stubStateInspectorService = new StubStateHolder();
    
    private DumpListFormatterUtil stubDumpListFormatterUtil = new StubDumpListFormatterUtil();
    
    @Before
    public void setUp() {
        this.stateController = new StateController(stubStateInspectorService, this.stubDumpListFormatterUtil);
        this.request = new MockMultipartHttpServletRequest();
    }

    //BUNDLES
    
    /**
     * @throws ServletRequestBindingException from bundles()
     */
    @Test
    public void testBundles() throws ServletRequestBindingException {
        ModelAndView mav = this.stateController.bundles(this.request);
        Map<String, Object> model = mav.getModel();

        assertEquals("state-bundles", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertEquals("Live", model.get("state"));
    }
    
    /**
     * @throws ServletRequestBindingException from bundles()
     */
    @Test
    public void testBundlesWithState() throws ServletRequestBindingException {
        String formattedTestDump = String.valueOf(System.currentTimeMillis());
        request.addParameter("state", formattedTestDump);
        ModelAndView mav = this.stateController.bundles(this.request);
        Map<String, Object> model = mav.getModel();

        assertEquals("state-bundles", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertEquals(formattedTestDump, model.get("state"));
    }

    //SERVICES
    
    @Test
    public void testServices() throws ServletRequestBindingException{
        ModelAndView mav = this.stateController.services(this.request);
        Map<String, Object> model = mav.getModel();
        assertEquals("state-services", mav.getViewName());
        assertTrue(model.containsKey("services"));
    }
    
    //BUNDLE
    
    /**
     * @throws ServletRequestBindingException from bundle()
     */
    @Test
    public void testBundle() throws ServletRequestBindingException {
        this.request.addParameter("id", "4");
        ModelAndView mav = this.stateController.bundle(this.request);
        Map<String, Object> model = mav.getModel();

        assertEquals("state-bundle", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertTrue(model.containsKey("title"));
        assertTrue(model.containsKey("bundle"));
    }

    /**
     * @throws ServletRequestBindingException from bundle()
     */
    @Test
    public void testBundleNotExist() throws ServletRequestBindingException {
        this.request.addParameter("id", "5");
        ModelAndView mav = this.stateController.bundle(this.request);
        Map<String, Object> model = mav.getModel();

        assertEquals("state-bundle", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertTrue(model.containsKey("title"));
        assertFalse(model.containsKey("bundle"));
    }

    /**
     * @throws ServletRequestBindingException from bundle() 
     */
    @Test
    public void testBundleNoBundle() throws ServletRequestBindingException {
        ModelAndView mav = this.stateController.bundle(this.request);
        Map<String, Object> model = mav.getModel();

        assertEquals("state-bundle", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertTrue(model.containsKey("title"));
    }

    //PACKAGES
    
    /**
     * @throws ServletRequestBindingException from packages()
     */
    @Test
    public void testPackages() throws ServletRequestBindingException {
        this.request.addParameter("name", "com.foo.bar.test");
        ModelAndView mav = this.stateController.packages(request);
        Map<String, Object> model = mav.getModel();
        
        assertEquals("state-packages", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertTrue(model.containsKey("title"));
        assertTrue(model.containsKey("importers"));
        assertTrue(model.containsKey("exporters"));
    }

    /**
     * @throws ServletRequestBindingException from packages() 
     */
    @Test
    public void testPackagesWithNoPackage() throws ServletRequestBindingException {
        ModelAndView mav = this.stateController.packages(request);
        Map<String, Object> model = mav.getModel();
        
        assertEquals("state-packages", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertTrue(model.containsKey("title"));
        assertFalse(model.containsKey("importers"));
        assertFalse(model.containsKey("exporters"));
    }
    
    //RESOLVE
    
    @Test
    public void testResolveStateExists() throws ServletRequestBindingException {
        this.request.addParameter("id", "4");
        String formattedTestDump = String.valueOf(System.currentTimeMillis());
        request.addParameter("state", formattedTestDump);
        ModelAndView mav = this.stateController.resolve(request);
        Map<String, Object> model = mav.getModel();
        
        assertEquals("state-resolve", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertEquals(formattedTestDump, model.get("state"));
        
        assertTrue(model.containsKey("failure"));
        assertTrue(model.containsKey("title"));
    }
    
    @Test
    public void testResolveExists() throws ServletRequestBindingException {
        this.request.addParameter("id", "4");
        ModelAndView mav = this.stateController.resolve(request);
        Map<String, Object> model = mav.getModel();
        
        assertEquals("state-resolve", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertEquals("Live", model.get("state"));
        
        assertTrue(model.containsKey("failure"));
        assertTrue(model.containsKey("title"));
    }
    
    @Test
    public void testResolveNotExists() throws ServletRequestBindingException {
        this.request.addParameter("id", "5");
        ModelAndView mav = this.stateController.resolve(request);
        Map<String, Object> model = mav.getModel();
        assertEquals("state-resolve", mav.getViewName());
        
        assertFalse(model.containsKey("failure"));
        assertTrue(model.containsKey("title"));
    }
    
    @Test
    public void testResolveNoBundle() throws ServletRequestBindingException {
        ModelAndView mav = this.stateController.resolve(request);
        Map<String, Object> model = mav.getModel();
        
        assertEquals("state-resolve", mav.getViewName());
        
        assertFalse(model.containsKey("failure"));
        assertTrue(model.containsKey("title"));
    }
    
    //SEARCH
        
    @Test
    public void testSearch() throws ServletRequestBindingException {
        ModelAndView mav = this.stateController.search(request);
        Map<String, Object> model = mav.getModel();
        
        assertEquals("state-search", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertEquals("Live", model.get("state"));
        
        assertFalse(model.containsKey("bundles"));
        assertTrue(model.containsKey("title"));
    }
        
    @Test
    public void testSearchWithTerm() throws ServletRequestBindingException {
        this.request.addParameter("term", "com.**.whatever");
        ModelAndView mav = this.stateController.search(request);
        Map<String, Object> model = mav.getModel();
        
        assertEquals("state-search", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertEquals("Live", model.get("state"));
        
        assertTrue(model.containsKey("bundles"));
        assertTrue(model.containsKey("title"));
    }
        
    @Test
    public void testSearchState() throws ServletRequestBindingException {
        String formattedTestDump = String.valueOf(System.currentTimeMillis());
        request.addParameter("state", formattedTestDump);
        ModelAndView mav = this.stateController.search(request);
        Map<String, Object> model = mav.getModel();
        
        assertEquals("state-search", mav.getViewName());
        assertTrue(model.containsKey("stateSources"));
        assertTrue(model.containsKey("state"));
        assertEquals(formattedTestDump, model.get("state"));
        
        assertTrue(model.containsKey("title"));
    }
    
}
