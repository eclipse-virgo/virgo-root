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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import tld.listeners.Account;

public class ContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("Context Destroyed");
	}

	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		String uname = context.getInitParameter("username");
		String pwd = context.getInitParameter("password");
		context.setAttribute("accountInfo", new Account(uname, pwd));

		System.out.println("Context Intialized");
	}
}
