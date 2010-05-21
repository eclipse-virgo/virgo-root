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

import java.util.Map;

import org.eclipse.virgo.apps.admin.web.AppController;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.web.servlet.ModelAndView;

/**
 */
public class AppControllerTests {

    private AppController appController;
    
    @Before
    public void setUp() {
        this.appController = new AppController();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInformation(){
        ModelAndView information = this.appController.overview();
        assertTrue(information.hasView());
        assertTrue(information.getViewName().equals("info-overview"));
        assertTrue(information.getModel().containsKey("properties"));
        Object object = information.getModel().get("properties");
        Map<String, String> props = (Map<String, String>) object;
        assertEquals(5, props.size());
    }
    
}
