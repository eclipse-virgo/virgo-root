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

import org.eclipse.virgo.apps.admin.web.stubs.common.Server;
import org.eclipse.virgo.apps.admin.web.stubs.moo.Element;
import org.eclipse.virgo.apps.admin.web.stubs.moo.Fx;
import org.eclipse.virgo.apps.admin.web.stubs.moo.HtmlTable;
import org.eclipse.virgo.apps.admin.web.stubs.moo.Spinner;
import org.eclipse.virgo.apps.admin.web.stubs.browser.Window;
import org.eclipse.virgo.apps.admin.web.stubs.common.Util;
import org.eclipse.virgo.apps.admin.web.stubs.moo.Request;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.FunctionObject;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 *
 */
public abstract class AbstractJSTests {
	
	protected static Context CONTEXT; 
	
	protected static ScriptableObject SCOPE;
	
	protected static String dollarLookup = "";
	
	protected static ScriptableObject dollarLookupToReturn = null;
	
	protected Util commonUtil;
	
	protected Server commonServer;
	
	protected Window window;
	
	@BeforeClass
	public static void setUp() throws ScriptException, IOException, IllegalAccessException, InstantiationException, InvocationTargetException, SecurityException, NoSuchMethodException{
		//printEngine();
		CONTEXT = Context.enter();
		SCOPE = CONTEXT.initStandardObjects();

		//Create the browser environment
		ScriptableObject.defineClass(SCOPE, HtmlTable.class);
		ScriptableObject.defineClass(SCOPE, Request.class);
		ScriptableObject.defineClass(SCOPE, Fx.class);
		ScriptableObject.defineClass(SCOPE, Spinner.class);
		ScriptableObject.defineClass(SCOPE, Element.class);
		ScriptableObject.putProperty(SCOPE, "window", Context.javaToJS(new Window(), SCOPE));
		
		//Add in constructed objects
		CONTEXT.evaluateReader(SCOPE, new FileReader("src/test/resources/MooStub.js"), "src/test/resources/MooStub.js", 0, null); 
		
		FunctionObject dollarFunction = new FunctionObject("$", AbstractJSTests.class.getDeclaredMethod("dollar", Object.class), SCOPE);
		ScriptableObject.putProperty(SCOPE, dollarFunction.getFunctionName(), dollarFunction);
	}
	
	@AfterClass
	public static void closeDown(){
		Context.exit();
	}
	
	public static Object dollar(Object name){
		dollarLookup = (String) Context.jsToJava(name, String.class);
		if(dollarLookupToReturn != null){
			return dollarLookupToReturn;
		}
		return name;
	}
	
	protected final Object addObject(Object object, String name){
		Object wrapped = Context.javaToJS(object, SCOPE);
		ScriptableObject.putProperty(SCOPE, name, wrapped);
		return wrapped;
	}
	
	protected final void readFile(String fileName) throws IOException{
		CONTEXT.evaluateReader(SCOPE, new FileReader(fileName), fileName, 0, null);
	}
	
	protected final void addCommonObjects(){
		this.commonUtil = new Util(SCOPE);
		addObject(commonUtil, "Util");
		this.commonServer = new Server();
		addObject(commonServer, "Server");
	}
	
	protected final void invokePageInit() throws ScriptException, NoSuchMethodException{
		Object fObj = SCOPE.get("pageinit", SCOPE);
		if (fObj instanceof Function) {
		    Function f = (Function)fObj;
		    f.call(CONTEXT, SCOPE, SCOPE, Context.emptyArgs);
		} else {
			Assert.fail("pageinit function not found");
		}		
	}
	
//	private static void printEngine(){
//		ScriptEngineManager engineManager = new ScriptEngineManager();
//		ScriptEngine engine = engineManager.getEngineByName("rhino");
//
//		System.out.println(String.format("Running on %s-%s", engine.getFactory().getEngineName(), engine.getFactory().getEngineVersion()));
//		System.out.println(String.format("Language %s-%s", engine.getFactory().getLanguageName(), engine.getFactory().getLanguageVersion()));
//	}

}
