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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import javax.script.ScriptException;

import org.eclipse.virgo.management.console.stubs.objects.ObjectName;
import org.eclipse.virgo.management.console.stubs.types.Element;
import org.junit.Ignore;
import org.junit.Test;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

/**
 *
 *
 */
public class ConfigurationJSTests extends AbstractJSTests {
	
	/**
	 * Test that the init script for the page reports back that the page is ready for display
	 * 
	 * @throws ScriptException
	 * @throws IOException
	 * @throws NoSuchMethodException
	 */
	@Test
	@Ignore
	public void testPageinit() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/configuration.js");

		invokePageInit();
		assertNotNull(commonUtil.getLastQueryCallBack());

		commonUtil.getLastQueryCallBack().call(context, scope, scope, new Object[]{getTestData()});
		assertTrue("Page ready has not been called", commonUtil.isPageReady());
	}
	
	/**
	 * Tests that the css class applied to the twisty changes from plus to minus as toggle is called.
	 * @throws IOException 
	 * 
	 */
	@Test
	@Ignore
	public void testConfigToggle() throws IOException {
		Function configurationConstructor = (Function) scope.get("Configuration", scope);
		
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("name", "testName");
		ObjectName value = new ObjectName("domain", "objectNameString", properties);

		Scriptable labelElement = ((Function) scope.get("Element", scope)).construct(context, scope, new Object[]{"<div />"});
		Scriptable configuration = configurationConstructor.construct(context, scope, new Object[]{Context.javaToJS(value, scope), labelElement});

		assertFalse("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("plus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("minus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("spinnerIcon"));

		Function toggleFunction = (Function) configuration.get("toggle", scope);
		toggleFunction.call(context, scope, configuration, Context.emptyArgs); //Close it
		assertTrue("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("plus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("minus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("spinnerIcon"));
		
		toggleFunction.call(context, scope, configuration, Context.emptyArgs); //Open it
		assertTrue("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("spinnerIcon"));

		commonUtil.getLastQueryCallBack().call(context, scope, configuration, new Object[]{getTestData()});
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("plus"));
		assertTrue("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("minus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", scope)).jsFunction_hasClass("spinnerIcon"));
	}
	
	private Scriptable getTestData() throws IOException{
		
		readString( "var Data = function() {" +
					"	this.value = {};" +
					"	this.value.Value = {};" +
					"};");
		
		Function testData = (Function) scope.get("Data", scope);
		return testData.construct(context, scope, Context.emptyArgs);
	}
	
}
