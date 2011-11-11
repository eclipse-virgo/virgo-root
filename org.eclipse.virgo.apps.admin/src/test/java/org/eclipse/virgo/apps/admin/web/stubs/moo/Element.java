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

import java.util.HashSet;
import java.util.Set;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 *
 */
public class Element extends ParentStub {

	private static final long serialVersionUID = 1L;
	
	private String appendedText = null;

	private Set<String> class_names = new HashSet<String>();
	
	private boolean submitCalled = false;
	
	private static Object injectedInto = null;
	
	private static Set<String> constructedElements = new HashSet<String>();
	
	/**
	 * Prototype constructor
	 */
	public Element() {
	}
	
	/**
	 * JavaScript Constructor
	 */
	public Element(ScriptableObject name) {
		constructedElements.add((String) Context.jsToJava(name, String.class));
	}
	
	public void jsFunction_addClass(String className){
		this.class_names.add(className);
	}
	
	public void jsFunction_removeClass(String className){
		this.class_names.remove(className);
	}
	
	public void jsFunction_set(String property, Object propertyValues){
		
	}
	
	public void jsFunction_setProperty(String property, Object propertyValues){
		
	}
	
	public void jsFunction_replaces(Element oldElement){
		
	}
	
	public void jsFunction_submit(){
		this.submitCalled = true;
	}

	public boolean jsFunction_hasClass(String className){
		return this.class_names.contains(className);
	}
	
	public void jsFunction_inject(Object intoThisOtherElement){
		injectedInto = intoThisOtherElement;
	}

	public void jsFunction_reveal(){
		
	}

	public void jsFunction_empty(){
		
	}
	
	public void jsFunction_appendText(String text){
		this.appendedText = text;
	}
	
	public void jsFunction_destroy(Object oldElement){
		
	}
	
	public ScriptableObject[] jsFunction_getChildren(){
		return new ScriptableObject[]{this};
	}
	
	public ScriptableObject jsGet_firstChild(){
		return this;
	}
	
	// Test methods
	
	public static Set<String> getNames(){
		return constructedElements;
	}
	
	public Set<String> getClassNames(){
		return this.class_names;
	}
	
	public String getAppendedText(){
		return this.appendedText;
	}

	public static Object getInjectedInto() {
		return injectedInto;
	}
	
	public boolean isSubmitted(){
		return this.submitCalled;
	}
	
}
