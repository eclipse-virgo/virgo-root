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
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 *
 */
public class HtmlTable extends ParentStub {

	private static final long serialVersionUID = 1L;
	
	public static String[] row_data;

	public static String REPLACED_NODE_NAME;
	
	/**
	 * Prototype constructor
	 */
	public HtmlTable() {
	}
	
	/**
	 * JavaScript Constructor
	 */
	public HtmlTable(ScriptableObject options) {
		row_data = (String[]) Context.jsToJava(ScriptableObject.getProperty(options, "rows"), String[].class);
	}
	
	public void jsFunction_replaces(Object domNode){
		REPLACED_NODE_NAME = (String) Context.jsToJava(domNode, String.class);
	}

	public void jsFunction_inject(Object intoThisOtherElement){
		
	}
	
}
