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
package org.eclipse.virgo.apps.admin.web.stubs.moo;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.FunctionObject;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 *
 */
public class Request extends ParentStub {

	private static final long serialVersionUID = 1L;
	
	private static Scriptable global_scope = null;
	
	private Function onSuccess;

	private String Url;
	
	private static Function LAST_SENT_ON_SUCCESS;
	
	private static String LAST_SENT_URL;
	
	/**
	 * Test helper method.
	 */
	public static Function getLastSentOnSuccess() {
	    return LAST_SENT_ON_SUCCESS;
	}
	
	/**
     * Test helper method.
     */
	public static String getLastSentUrl() {
	    return LAST_SENT_URL;
	}
	
	/**
	 * Prototype constructor
	 */
	public Request() {
	}
	
	/**
	 * JavaScript Constructor
	 */
	public Request(ScriptableObject options) {
		this.Url = (String) ScriptableObject.getProperty(options, "url");
		this.onSuccess = (Function) ScriptableObject.getProperty(options, "onSuccess");
	}
	
	/**
	 * Need to store the global scope so we can create extensions of this class from it
	 * 
	 * @param scope
	 * @param constructor
	 * @param prototype
	 */
	public static void finishInit(Scriptable scope, FunctionObject constructor, Scriptable prototype){
		Request.global_scope = scope;
	}
	
	public void jsFunction_send(){
	    LAST_SENT_ON_SUCCESS = this.onSuccess;
	    LAST_SENT_URL = this.Url;
	}

	/**
	 * Just return a normal JS Request object, no need to differentiate  
	 * 
	 * @param options
	 * @return JS Request object
	 */
	public static ScriptableObject jsStaticFunction_JSON(ScriptableObject options){
		Function constructor = (Function)Request.global_scope.get(Request.class.getSimpleName(), Request.global_scope);
	    return (ScriptableObject) constructor.construct(Context.getCurrentContext(), constructor.getParentScope(), new Object[]{options});
	}
	
}
