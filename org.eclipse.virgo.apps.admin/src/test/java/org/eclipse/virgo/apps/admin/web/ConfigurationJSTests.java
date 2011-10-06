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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import javax.script.ScriptException;

import org.eclipse.virgo.apps.admin.web.stubs.common.ObjectName;
import org.eclipse.virgo.apps.admin.web.stubs.moo.Element;
import org.eclipse.virgo.apps.admin.web.stubs.moo.Request;
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
	public void testPageinit() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/configuration.js");

		invokePageInit();
		assertNotNull(Request.onSuccess);

		readFile("src/test/resources/ConfigurationData.js");
		Function jsonData = (Function) SCOPE.get("Data", SCOPE);
		Request.onSuccess.call(CONTEXT, SCOPE, SCOPE, new Object[]{jsonData.construct(CONTEXT, SCOPE, Context.emptyArgs)});
		assertTrue("Page ready has not been called", this.commonUtil.isPageReady());
	}
	
	/**
	 * Tests that the css class applied to the twisty changes from plus to minus as toggle is called.
	 * 
	 */
	@Test
	public void testConfigToggle() {
		Function configurationConstructor = (Function) SCOPE.get("Configuration", SCOPE);
		
		HashMap<String, String> properties = new HashMap<String, String>();
		ObjectName value = new ObjectName("domain", "objectNameString", properties);

		Function treeLabel = (Function) SCOPE.get("Label", SCOPE);
		Scriptable constructedTreeLabel = treeLabel.construct(CONTEXT, SCOPE, Context.emptyArgs);
		
		Scriptable configuration = configurationConstructor.construct(CONTEXT, SCOPE, new Object[]{Context.javaToJS(value, SCOPE), constructedTreeLabel});
		Function toggleFunction = (Function) configuration.get("toggle", SCOPE);

		assertTrue("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("plus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("minus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("spinnerIcon"));

		Function jsonData = (Function) SCOPE.get("Data", SCOPE);
		
		toggleFunction.call(CONTEXT, SCOPE, configuration, Context.emptyArgs); //Open it
		assertTrue("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("spinnerIcon"));
		Request.onSuccess.call(CONTEXT, SCOPE, configuration, new Object[]{jsonData.construct(CONTEXT, SCOPE, Context.emptyArgs)});
		assertTrue("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("minus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("plus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("spinnerIcon"));
		
		toggleFunction.call(CONTEXT, SCOPE, configuration, Context.emptyArgs); //Close it
		assertTrue("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("plus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("minus"));
		assertFalse("Icon not toggled", ((Element)configuration.get("icon", SCOPE)).jsFunction_hasClass("spinnerIcon"));
	}
	
}
