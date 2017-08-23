/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Violeta Georgieva - initial contribution
 *******************************************************************************/

package test.web;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestServlet
 */
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public TestServlet() {
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources("test.resource");
        int size = 0;
        while (urls.hasMoreElements()) {
            size++;
            response.getWriter().println("URL: " + urls.nextElement());
        }
        response.getWriter().println("Size: " + size);
   }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
