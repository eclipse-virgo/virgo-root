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



import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;



import org.springframework.web.servlet.ModelAndView;

import org.springframework.web.servlet.mvc.Controller;



public class ExampleController implements Controller {



    private String stringFromSpringDM;



    public void setStringFromSpringDM(String stringFromSpringDM) {

        this.stringFromSpringDM = stringFromSpringDM;

    }



    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        return new ModelAndView("test").addObject("stringFromSpringDM", this.stringFromSpringDM);

    }



}

