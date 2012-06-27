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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>
 * ConfigAdminController deals with requests for configuration information from config admin.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * ConfigAdminController is threadsafe
 *
 */
@Controller
public final class ConfigAdminController {
    
    private final ConfigurationAdmin configurationAdmin;
	
	/**
	 * Simple constructor taking an {@link ConfigurationAdmin} instance to provide any data required to render requests
	 * @param configurationAdmin data for request rendering 
	 */
    @Autowired
	public ConfigAdminController(ConfigurationAdmin configurationAdmin) {
		this.configurationAdmin = configurationAdmin;
	}

    /**
	 * Custom handler for displaying all the present configuration
	 * 
	 * @return ModelAndView to render
	 */
    @SuppressWarnings("unchecked")
    @RequestMapping("/config/overview.htm")
    public ModelAndView overview() {
		Configuration[] configurations = new Configuration[0];
        try {
            configurations = this.configurationAdmin.listConfigurations(null);
        } catch (Exception e) {
            // no-op
        }
        Map<String, Map<String, String>> configs = new HashMap<String, Map<String,String>>();
        if(configurations != null){
            Map<String, String> tempProps;
            for(Configuration c : configurations){
                tempProps = new HashMap<String, String>();
                Enumeration<String> keys = c.getProperties().keys();
                while(keys.hasMoreElements()){
                    String nextElement = keys.nextElement();
                    tempProps.put(nextElement.toString(), c.getProperties().get(nextElement).toString());
                }
                configs.put(c.getPid(), tempProps);
            }
        }
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("configurations", configs);
		return createStateModelAndView("config-overview", model);		
	}

    /**
     * Add any common model items and return a {@link ModelAndView}
     * @throws ServletRequestBindingException 
     */
	private ModelAndView createStateModelAndView(String viewName, Map<String, Object> model) {
		return new ModelAndView(viewName, model);
	}

	
}
