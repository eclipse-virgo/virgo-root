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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestDispatcherTestServlet extends HttpServlet {

	private static final long serialVersionUID = 4167184064530582217L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String test = request.getParameter("test");
		doTest(test, request, response);
	}

	private void doTest(String test, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if ("forward-jsp".equals(test)) {
			doJspForward(request, response);
		} else if ("forward-servlet".equals(test)) {
			doServletForward(request, response);
		} else if ("forward-path-translation".equals(test)) {
			doPathMappingForward(request, response);
		} else if ("include-jsp".equals(test)) {
			doJspInclude(request, response);
		} else if ("include-servlet".equals(test)) {
			doServletInclude(request, response);
		} else if ("include-path-translation".equals(test)) {
			doPathMappingInclude(request, response);
		} else if ("path-translation".equals(test)) {
			doPathTranslation(request, response);
		} else if ("path-exact".equals(test)) {
			doPathExactTranslation(request, response);
		} else if ("forward-filter-servlet".equals(test)) {
			doFilterServletForward(request, response);
		}
		else if ("include-filter-servlet".equals(test)) {
			doFilterServletInclude(request, response);
		}
	}

	private void doPathTranslation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/path/1");
		requestDispatcher.forward(request, response);
	}

	private void doPathExactTranslation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/exact");
		requestDispatcher.forward(request, response);
	}

	private void doJspForward(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/index.jsp");
		requestDispatcher.forward(request, response);
	}

	private void doServletForward(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/forward");
		requestDispatcher.forward(request, response);
	}

	private void doFilterServletForward(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/testFilterOne");
		requestDispatcher.forward(request, response);
	}

	private void doServletInclude(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/include");
		requestDispatcher.include(request, response);
	}

	private void doPathMappingForward(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/fa/1");
		requestDispatcher.forward(request, response);
	}

	private void doFilterServletInclude(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/testFilterTwo");
		requestDispatcher.include(request, response);
	}

	private void doPathMappingInclude(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/ia/1");
		requestDispatcher.include(request, response);
	}

	private void doJspInclude(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher("/index.jsp");
		requestDispatcher.include(request, response);
	}

}
