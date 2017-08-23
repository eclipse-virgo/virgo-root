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

public class IncludeAttributesServlet extends HttpServlet {

	private static final long serialVersionUID = 2553682795358600889L;

	private static final String INCLUDE_REQUEST_URI_ATTRIBUTE_NAME = "javax.servlet.include.request_uri";

	private static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE_NAME = "javax.servlet.include.context_path";

	private static final String INCLUDE_SERVLET_PATH_ATTRIBUTE_NAME = "javax.servlet.include.servlet_path";

	private static final String INCLUDE_PATH_INFO_ATTRIBUTE_NAME = "javax.servlet.include.path_info";

	private static final String INCLUDE_QUERY_STRING_ATTRIBUTE_NAME = "javax.servlet.include.query_string";

	private static final String REQUEST_URI = "requestURI";

	private static final String CONTEXT_PATH = "ContextPath";

	private static final String SERVLET_PATH = "ServletPath";

	private static final String PATH_INFO = "PathInfo";

	private static final String QUERY_STRING = "QueryString";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();

		writer.println(INCLUDE_REQUEST_URI_ATTRIBUTE_NAME + ":"
				+ req.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE_NAME));
		writer.println(INCLUDE_CONTEXT_PATH_ATTRIBUTE_NAME + ":"
				+ req.getAttribute(INCLUDE_CONTEXT_PATH_ATTRIBUTE_NAME));
		writer.println(INCLUDE_SERVLET_PATH_ATTRIBUTE_NAME + ":"
				+ req.getAttribute(INCLUDE_SERVLET_PATH_ATTRIBUTE_NAME));
		writer.println(INCLUDE_PATH_INFO_ATTRIBUTE_NAME + ":"
				+ req.getAttribute(INCLUDE_PATH_INFO_ATTRIBUTE_NAME));
		writer.println(INCLUDE_QUERY_STRING_ATTRIBUTE_NAME + ":"
				+ req.getAttribute(INCLUDE_QUERY_STRING_ATTRIBUTE_NAME));

		writer.println(REQUEST_URI + ":" + req.getRequestURI());
		writer.println(CONTEXT_PATH + ":" + req.getContextPath());
		writer.println(SERVLET_PATH + ":" + req.getServletPath());
		writer.println(PATH_INFO + ":" + req.getPathInfo());
		writer.println(QUERY_STRING + ":" + req.getQueryString());

		writer.flush();
	}
}
