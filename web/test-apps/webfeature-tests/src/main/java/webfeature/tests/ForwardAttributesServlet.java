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

package webfeature.tests;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForwardAttributesServlet extends HttpServlet {
    
    private static final long serialVersionUID = -5733587568625399838L;

    private static final String FORWARD_REQUEST_URI_ATTRIBUTE_NAME = "javax.servlet.forward.request_uri";

    private static final String FORWARD_CONTEXT_PATH_ATTRIBUTE_NAME = "javax.servlet.forward.context_path";

    private static final String FORWARD_SERVLET_PATH_ATTRIBUTE_NAME = "javax.servlet.forward.servlet_path";

    private static final String FORWARD_PATH_INFO_ATTRIBUTE_NAME = "javax.servlet.forward.path_info";

    private static final String FORWARD_QUERY_STRING_ATTRIBUTE_NAME = "javax.servlet.forward.query_string";
    
    private static final String REQUEST_URI = "requestURI";

    private static final String CONTEXT_PATH = "ContextPath";

    private static final String SERVLET_PATH = "ServletPath";

    private static final String PATH_INFO = "PathInfo";

    private static final String QUERY_STRING = "QueryString";

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        writer.println(FORWARD_REQUEST_URI_ATTRIBUTE_NAME + ":" + req.getAttribute(FORWARD_REQUEST_URI_ATTRIBUTE_NAME));
        writer.println(FORWARD_CONTEXT_PATH_ATTRIBUTE_NAME + ":" + req.getAttribute(FORWARD_CONTEXT_PATH_ATTRIBUTE_NAME));
        writer.println(FORWARD_SERVLET_PATH_ATTRIBUTE_NAME+ ":" + req.getAttribute(FORWARD_SERVLET_PATH_ATTRIBUTE_NAME));
        writer.println(FORWARD_PATH_INFO_ATTRIBUTE_NAME + ":" + req.getAttribute(FORWARD_PATH_INFO_ATTRIBUTE_NAME));
        writer.println(FORWARD_QUERY_STRING_ATTRIBUTE_NAME + ":" + req.getAttribute(FORWARD_QUERY_STRING_ATTRIBUTE_NAME));
        
        writer.println(REQUEST_URI + ":" + req.getRequestURI());
        writer.println(CONTEXT_PATH + ":" + req.getContextPath());
        writer.println(SERVLET_PATH + ":" + req.getServletPath());
        writer.println(PATH_INFO + ":" + req.getPathInfo());
        writer.println(QUERY_STRING + ":" + req.getQueryString());
        
        writer.flush();
    }
}
