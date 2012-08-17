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
package org.eclipse.virgo.jetty.sample.web;


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
public final class SampleController {

	private final FormHandler formHandler = new FormHandler();
	
    /**
     * Custom handler for displaying a list of properties.
     * @return ModelAndView to render
     */
    @RequestMapping("/sample.htm")
    public ModelAndView overview(String comment) {
    	if(comment == null || comment.length() == 0){
    		comment = "None.";
    	}
        return new ModelAndView("sample").addObject("commentHandler", formHandler).addObject("lastComment", comment);
    }

}
