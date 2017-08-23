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
import javax.servlet.http.HttpSession;

public class RequestListenerTestServlet extends HttpServlet{

	private static final long serialVersionUID = -3540533331917837472L;

	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
		HttpSession session=request.getSession();
		PrintWriter out=response.getWriter();
		out.println("<html><body><h1>Request Listener Test Servlet</h1></body></html>");
		out.println("<b><h2>Product Name: " + session.getAttribute("product") + "</h2></b>");
	}
}
