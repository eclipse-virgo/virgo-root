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
package org.eclipse.virgo.apps.admin.web.internal;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * 
 *
 */
public class AdminHttpContext implements HttpContext {

//	private static final Logger log = LoggerFactory.getLogger(AdminHttpContext.class);
	
	private final Bundle bundle;
	
//	private final AccessControlContext accessControlContext;

	public AdminHttpContext(Bundle bundle) {
//		this.accessControlContext = AccessController.getContext();
		this.bundle = bundle;
	}
	
	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return true;//adminSecurityTracker.handleSecurity(request, response);
	}

	@Override
	public URL getResource(final String name) {
		//log.warn("AdminHttpContext getResource " + name);
		return bundle.getEntry(name);
//		if (System.getSecurityManager() == null){
//		     return bundle.getResource(name);
//		}
//		return (URL) AccessController.doPrivileged(new PrivilegedAction<Object>() {
//			public Object run() {
//				return bundle.getEntry(name);
//			}
//		}, accessControlContext);
	}

	@Override
	public String getMimeType(String name) {
		return null;
	}

}
