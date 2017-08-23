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

package tld.listeners;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tld.listeners.Account;

public class ContextListenerTestServlet extends HttpServlet {

	private static final long serialVersionUID = -5245101730050164872L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out
				.println("<html><body><h1>Context Listener Test Servlet</h1></body></html>");
		Account account = (Account) getServletContext().getAttribute(
				"accountInfo");
		if (account != null) {
			out.println("<b><h2>Username: " + account.username + "</h2></b>");
			out.println("<b><h2>Password: " + account.password + "</h2></b>");
		}
	}

}
