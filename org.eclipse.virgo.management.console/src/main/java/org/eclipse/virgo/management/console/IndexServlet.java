/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.management.console;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A very simple servlet to redirect requests to the root servlet path to the overview page
 *
 */
public class IndexServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final ContentServlet contentServlet;
	
	public IndexServlet(ContentServlet contentServlet) {
		this.contentServlet = contentServlet;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.contentServlet.doGet(request, response);
	}
	
}
