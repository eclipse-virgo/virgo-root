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

package sct;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.XmlWebApplicationContext;

public class ApplicationContextNamespaceTestServlet extends HttpServlet {

    private static final long serialVersionUID = -3223361902099778097L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        XmlWebApplicationContext ctx = new XmlWebApplicationContext();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ctx.setClassLoader(loader);
        ctx.setConfigLocation("classpath:sct/namespace.xml");
        ctx.refresh();
        Object bean = ctx.getBean("someBean");
        if (bean != null) {
            PrintWriter writer = resp.getWriter();
            writer.write("OK");
            writer.flush();
        }
    }

}
