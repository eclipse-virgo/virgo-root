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
package org.eclipse.virgo.management.console;

import org.eclipse.virgo.management.console.stubs.objects.Dollar;
import org.eclipse.virgo.management.console.stubs.objects.Util;
import org.eclipse.virgo.management.console.stubs.objects.Window;
import org.eclipse.virgo.management.console.stubs.types.Element;
import org.eclipse.virgo.management.console.stubs.types.Server;
import org.junit.After;
import org.junit.Before;
import org.mozilla.javascript.*;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.fail;

public abstract class AbstractJSTests {
	
	protected Context context; 
	
	protected ScriptableObject scope;

	static String alertMsg;
	
	Util commonUtil = null;
	
	@Before
	public void setUp() throws IllegalAccessException, InstantiationException, InvocationTargetException, SecurityException, NoSuchMethodException{
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
		ScriptableObject.putProperty(dollarFunction, "browser", Context.javaToJS(new Object(), scope));
		ScriptableObject.putProperty(dollarFunction, "each", new FunctionObject("each", Dollar.class.getDeclaredMethod("each", Scriptable.class, Function.class), dollarFunction));
		
		FunctionObject alertFunction = new FunctionObject("alert", AbstractJSTests.class.getDeclaredMethod("alert", String.class), scope);
		ScriptableObject.putProperty(scope, alertFunction.getFunctionName(), alertFunction);
	}

	@After
	public void closeDown(){
		Context.exit();
	}
	
	// callback from JS function doesn't work with non static field
	private static void alert(String msg){
		System.out.println(msg);
		alertMsg = msg;
	}
	
	protected final void readFile(String fileName) throws IOException{
		context.evaluateReader(scope, new FileReader(fileName), fileName, 0, null);
	}
	
	final void readString(String js) {
		context.evaluateString(scope, js, "snippet", 0, null);
	}
	
	final void addCommonObjects(){
		commonUtil = new Util(context, scope);
		Object wrapped = Context.javaToJS(commonUtil, scope);
		ScriptableObject.putProperty(scope, "util", wrapped);
	}
	
	final void invokePageInit() {
		Object fObj = scope.get("pageinit", scope);
		if (fObj instanceof Function) {
		    Function f = (Function)fObj;
		    f.call(context, scope, scope, Context.emptyArgs);
		} else {
			fail("pageinit function not found");
		}		
	}

}
