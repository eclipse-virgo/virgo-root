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

import org.eclipse.virgo.apps.admin.web.stubs.objects.Util;

import junit.framework.Assert;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.FunctionObject;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 *
 */
public class Fx extends ParentStub {

	private static final long serialVersionUID = 1L;
	
	private static Scriptable global_scope = null;
	
	public static ScriptableObject element;

	public static int duration;
	
	/**
	 * Prototype constructor
	 */
	public Fx() {
	}
	
	/**
	 * JavaScript Constructor
	 */
	public Fx(ScriptableObject elementToChange, ScriptableObject options) {
		//duration = (Integer) ScriptableObject.getProperty(options, "duration");
		element = elementToChange;
	}
	
	/**
	 * Need to store the global scope so we can create extensions of this class from it
	 * 
	 * @param scope
	 * @param constructor
	 * @param prototype
	 */
	public static void finishInit(Scriptable scope, FunctionObject constructor, Scriptable prototype){
		Fx.global_scope = scope;
	}

	public ScriptableObject jsFunction_dissolve(){
		return this;
	}
	
	public void jsFunction_toggle(){
	}
	
	/**
	 * Just return a normal JS Request object, no need to differentiate  
	 * 
	 * @param options
	 * @return
	 */
	public static ScriptableObject jsStaticFunction_Reveal(ScriptableObject element, ScriptableObject options){
		Assert.assertEquals(Util.fxTime, Context.jsToJava(ScriptableObject.getProperty(options, "duration"), Integer.class));
		Function fObj = (Function) Fx.global_scope.get(Fx.class.getSimpleName(), Fx.global_scope);
		if (fObj instanceof Function) {
		    Function constructor = (Function)fObj;
		    return (ScriptableObject) constructor.construct(Context.getCurrentContext(), constructor.getParentScope(), new Object[]{element, options});
		} else {
			Assert.fail("Fx constructor not found");
			return null;
		}	
	}
	
}
