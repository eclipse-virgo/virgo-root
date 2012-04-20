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
package org.eclipse.virgo.apps.admin.web.stubs.types;

import sun.org.mozilla.javascript.internal.Function;

/**
 * 
 *
 */
public class Server extends ParentStub{

	private static final long serialVersionUID = 1L;
	
	private static Function callbackFunction;
	
	public Server() {
	}
	
	// Stub methods

	public void jsFunction_getServerOverview(Function callbackFunction){
		Server.callbackFunction = callbackFunction;
	}
	
	// Test methods

	public static Function getCallbackFunction() {
		return Server.callbackFunction;
	}
	
}
