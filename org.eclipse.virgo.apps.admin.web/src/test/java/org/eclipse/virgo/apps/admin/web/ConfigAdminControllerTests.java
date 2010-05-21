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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import org.eclipse.virgo.apps.admin.web.ConfigAdminController;
import org.eclipse.virgo.teststubs.osgi.service.cm.StubConfiguration;
import org.eclipse.virgo.teststubs.osgi.service.cm.StubConfigurationAdmin;


/**
 */
public class ConfigAdminControllerTests {

    private static final String TEST_PID = "test.pid";
    
    private ConfigAdminController configAdminController;

    @Before
    public void setUp() throws Exception {
        StubConfigurationAdmin configurationAdmin = new StubConfigurationAdmin();
        StubConfiguration stubConfiguration = configurationAdmin.createConfiguration("test.pid");
        stubConfiguration.addProperty("testKey", "testValue");
        this.configAdminController = new ConfigAdminController(configurationAdmin);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOverview() {
        ModelAndView overview = this.configAdminController.overview();
        assertNotNull(overview);
        Object object = overview.getModel().get("configurations");
        assertNotNull(object);
        Map<String, Map<String, String>> configs = (Map<String, Map<String, String>>) object;
        assertTrue(configs.containsKey(TEST_PID));
        assertTrue(configs.get(TEST_PID).containsKey("testKey"));
    }
    
}
