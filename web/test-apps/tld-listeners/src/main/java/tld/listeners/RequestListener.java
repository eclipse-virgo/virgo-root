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

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class RequestListener implements ServletRequestListener {

	public void requestDestroyed(ServletRequestEvent sre) {
		System.out.println("requestDestroyed");
	}

	public void requestInitialized(ServletRequestEvent sre) {
		HttpServletRequest request = (HttpServletRequest) sre
				.getServletRequest();
		HttpSession session = request.getSession(false);
		if (session == null) {
			HttpSession sess = request.getSession();
			sess.setAttribute("product", "virgo");
		} else {
			session.setAttribute("product", "virgo");
		}
	}
}
