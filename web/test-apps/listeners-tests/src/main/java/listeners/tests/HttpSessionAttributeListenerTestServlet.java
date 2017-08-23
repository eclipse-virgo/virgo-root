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

package listeners.tests;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HttpSessionAttributeListenerTestServlet extends HttpServlet {

	private static final long serialVersionUID = -3457743083868620475L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession(true);
		session.setAttribute("foo1", "bar1");
		session.setAttribute("foo2", "bar2");
		String str1 = (String) session.getAttribute("foo1");
		out.println("foo1: " + str1);
		String str2 = (String) session.getAttribute("foo2");
		out.println("foo2: " + str2);
		session.removeAttribute("foo2");
		session.setAttribute("foo1", "bar3");
	}

}
