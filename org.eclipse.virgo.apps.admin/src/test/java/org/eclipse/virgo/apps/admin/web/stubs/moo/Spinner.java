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

import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 *
 */
public class Spinner extends ParentStub {

	private static final long serialVersionUID = 1L;
	
	public static String hiddenElement;
	
	/**
	 * Prototype constructor
	 */
	public Spinner() {
	}
	
	/**
	 * JavaScript Constructor
	 */
	public Spinner(String elementToHide) {
		hiddenElement = elementToHide;
	}

	
	public void jsFunction_show(Boolean showFx){
	}

	public void jsFunction_hide(Boolean showFx){
	}

	public void jsFunction_addEvent(String name, ScriptableObject function){
	}
	
}
