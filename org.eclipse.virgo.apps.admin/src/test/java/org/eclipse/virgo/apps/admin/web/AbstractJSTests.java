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
package org.eclipse.virgo.apps.admin.web;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.script.ScriptException;

import junit.framework.Assert;

import org.eclipse.virgo.apps.admin.web.stubs.objects.Dollar;
import org.eclipse.virgo.apps.admin.web.stubs.objects.Util;
import org.eclipse.virgo.apps.admin.web.stubs.objects.Window;
import org.eclipse.virgo.apps.admin.web.stubs.types.Element;
import org.eclipse.virgo.apps.admin.web.stubs.types.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.FunctionObject;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 *
 */
public abstract class AbstractJSTests {
	
	protected static Context context; 
	
	protected static ScriptableObject scope;

	protected static String alertMsg;
	
	protected static Util commonUtil = null;
	
	@BeforeClass
	public static void setUp() throws ScriptException, IOException, IllegalAccessException, InstantiationException, InvocationTargetException, SecurityException, NoSuchMethodException{
		context = Context.enter();
		scope = context.initStandardObjects();

		//Create the browser environment
		ScriptableObject.defineClass(scope, Element.class);
		ScriptableObject.defineClass(scope, Server.class);
		ScriptableObject.putProperty(scope, "window", Context.javaToJS(new Window(), scope));
		
		Dollar.init(context, scope);
		FunctionObject dollarFunction = new FunctionObject("$", Dollar.class.getDeclaredMethod("dollar", ScriptableObject.class), scope);
		ScriptableObject.putProperty(scope, "$", dollarFunction);
		ScriptableObject.putProperty(dollarFunction, "ajax", new FunctionObject("ajax", Dollar.class.getDeclaredMethod("ajax", Scriptable.class), dollarFunction));
		ScriptableObject.putProperty(dollarFunction, "each", new FunctionObject("each", Dollar.class.getDeclaredMethod("each", Scriptable.class, Function.class), dollarFunction));
		
		FunctionObject alertFunction = new FunctionObject("alert", AbstractJSTests.class.getDeclaredMethod("alert", String.class), scope);
		ScriptableObject.putProperty(scope, alertFunction.getFunctionName(), alertFunction);
	}
	
	@AfterClass
	public static void closeDown(){
		Context.exit();
	}
	
	public static void alert(String msg){
		System.out.println(msg);
		alertMsg = msg;
	}
	
	protected final void readFile(String fileName) throws IOException{
		context.evaluateReader(scope, new FileReader(fileName), fileName, 0, null);
	}
	
	protected final void readString(String js) throws IOException{
		context.evaluateString(scope, js, "snippet", 0, null);
	}
	
	protected final void addCommonObjects(){
		commonUtil = new Util(context, scope);
		Object wrapped = Context.javaToJS(commonUtil, scope);
		ScriptableObject.putProperty(scope, "util", wrapped);
	}
	
	protected final void invokePageInit() throws ScriptException, NoSuchMethodException{
		Object fObj = scope.get("pageinit", scope);
		if (fObj instanceof Function) {
		    Function f = (Function)fObj;
		    f.call(context, scope, scope, Context.emptyArgs);
		} else {
			Assert.fail("pageinit function not found");
		}		
	}

}
