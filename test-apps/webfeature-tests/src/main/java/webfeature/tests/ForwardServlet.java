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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForwardServlet extends HttpServlet {

	private static final long serialVersionUID = -2633852286165467307L;

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		res.setContentType("text/plain");
		PrintWriter writer = res.getWriter();
		writer
				.write("Before forwarding the request to /fa in DispatcherServlet\n");
		RequestDispatcher dispatcher = req.getRequestDispatcher("/fa");
		if (dispatcher != null){
             dispatcher.forward(req, res);
		}

		writer.write("\nAfter forwarding the request to /fa in DispatcherServlet");

	}
}
