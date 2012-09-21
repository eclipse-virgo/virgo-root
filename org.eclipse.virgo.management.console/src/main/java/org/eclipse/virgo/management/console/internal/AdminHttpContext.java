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
package org.eclipse.virgo.management.console.internal;

import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.osgi.internal.signedcontent.Base64;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/**
 * 
 *
 */
public class AdminHttpContext implements HttpContext {
	
	private static final String REALM = "Virgo Admin Console";
	
	private final Bundle bundle;
	
	public AdminHttpContext(Bundle bundle) {
		this.bundle = bundle;
	}
	
	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String auth = request.getHeader("Authorization");
		if (auth == null) {
			return reject(request, response);
		}

		StringTokenizer tokens = new StringTokenizer(auth);
		String authscheme = tokens.nextToken();
		if (!authscheme.equals("Basic")) {
			return reject(request, response);
		}

		String base64credentials = tokens.nextToken();
		String credentials = new String(Base64.decode(base64credentials.getBytes()));
		int colon = credentials.indexOf(':');
		String userid = credentials.substring(0, colon);
		String password = credentials.substring(colon + 1);

		try {
			Subject subject = login(request.getSession(true), userid, password);
			if(subject == null){
				return reject(request, response);
			}
			request.setAttribute(HttpContext.REMOTE_USER, userid);
			request.setAttribute(HttpContext.AUTHENTICATION_TYPE, authscheme);
			request.setAttribute(HttpContext.AUTHORIZATION, subject);
			return true;
		} catch (LoginException e) {
			return reject(request, response);
		}
	}

	@Override
	public URL getResource(final String name) {
		return bundle.getEntry(name);
	}

	@Override
	public String getMimeType(String name) {
		return null;
	}
	
	@SuppressWarnings("deprecation")
	private Subject login(final HttpSession session, final String userid, final String password) throws LoginException {
		if (session == null){
			return null;
		}
		LoginContext context = (LoginContext) session.getValue("securitycontext");
		if (context != null){
			return context.getSubject();
		}	
		context = new LoginContext("virgo-kernel", new CallbackHandler() {
			
			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				for (int i = 0; i < callbacks.length; i++) {
					if (callbacks[i] instanceof NameCallback){
						((NameCallback) callbacks[i]).setName(userid);
					} else if (callbacks[i] instanceof PasswordCallback){
						((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
					} else {
						throw new UnsupportedCallbackException(callbacks[i]);
					}
				}
			}
			
		});
		context.login();
		Subject result = context.getSubject();
		if(result == null){
			return null;
		}
		session.putValue("securitycontext", context);
		return result;
	}
	
	private boolean reject(HttpServletRequest request, HttpServletResponse response) {
		request.getSession(true);
		response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
		try {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (IOException e) {
			// no-op
		}
		return false;
	}

}
