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

package example;



import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;



import org.springframework.web.context.ServletContextAware;

import org.springframework.web.context.WebApplicationContext;

import org.springframework.web.context.support.WebApplicationContextUtils;

import org.springframework.web.servlet.ModelAndView;

import org.springframework.web.servlet.mvc.Controller;



public class WacuController implements Controller, ServletContextAware {



    private ServletContext servletContext;



    public void setServletContext(ServletContext servletContext) {

        this.servletContext = servletContext;

    }



    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(this.servletContext);



        return new ModelAndView("wacu")//

        .addObject("applicationContextDisplayName", wac.getDisplayName())//

        .addObject("applicationContextClassName", wac.getClass().getName());

    }

}

