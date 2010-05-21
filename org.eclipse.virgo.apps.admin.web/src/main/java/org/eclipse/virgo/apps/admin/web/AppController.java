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
import java.util.TreeMap;

import org.eclipse.virgo.apps.admin.web.internal.AdminConsoleUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


/**
 * <p>
 * AppController deals with requests for the console landing page, general information view and links.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * AppController is threadsafe
 *
 */
@Controller
public final class AppController {

    private AdminConsoleUtil adminConsoleUtil = new AdminConsoleUtil();

    /**
     * Custom handler for displaying a list of properties.
     * @return ModelAndView to render
     */
    @RequestMapping("/info/overview.htm")
    public ModelAndView overview() {
        return new ModelAndView("info-overview").addObject("properties", this.getServerProperties());
    }

    private Map<String, String> getServerProperties() {
        Map<String, String> props = new TreeMap<String, String>();
        props.put("Virgo Server Version", this.adminConsoleUtil.getServerVersion());
        props.put("Operating System", this.adminConsoleUtil.getOperatingSystem());
        props.put("Java VM Description", this.adminConsoleUtil.getVMDesc());
        props.put("Java Version", this.adminConsoleUtil.getJavaDesc());
        props.put("Server Time Zone", this.adminConsoleUtil.getUserTimeZone());
        return props;
    }

}
