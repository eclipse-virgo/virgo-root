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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

/**
 *
 *
 */
public class WiringsJSTests extends AbstractJSTests {
	
	@Test
	public void testPageinit() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/wirings.js");
		createTestLayoutManager();
		createRaphael();
		
		invokePageInit();
		
		assertEquals("bundlesGui", commonUtil.getLastScriptLoaded());
		assertNotNull(commonUtil.getLoadScriptAsync());
		commonUtil.getLoadScriptAsync().call(context, scope, scope, new Object[]{});

		assertEquals("raphael", commonUtil.getLastScriptLoaded());
		assertNotNull(commonUtil.getLoadScriptAsync());
		commonUtil.getLoadScriptAsync().call(context, scope, scope, new Object[]{});
		
		assertNotNull(commonUtil.getLastQueryCallBack());
		commonUtil.getLastQueryCallBack().call(context, scope, scope, new Object[]{getTestValue()});
		
		assertNotNull(commonUtil.getLastBulkQueryCallBack());
		commonUtil.getLastBulkQueryCallBack().call(context, scope, scope, new Object[]{getTestValue()});
		
		assertTrue("Page ready has not been called", commonUtil.isPageReady());
	}
	
	@Test
	public void testConstructors() throws ScriptException, IOException, NoSuchMethodException{
		((Function) scope.get("SideBar", scope)).construct(context, scope, new Object[]{getTestLayoutManager(), null});
		((Function) scope.get("GeminiDataSource", scope)).construct(context, scope, new Object[]{});
	}

	private Scriptable getTestValue() throws IOException{
		readString( "var TestValue = function() {" +
					"	this.value = [];" +
					"};");
		Function testData = (Function) scope.get("TestValue", scope);
		return testData.construct(context, scope, Context.emptyArgs);
	}

	private void createTestLayoutManager() throws IOException{
		readString( "var LayoutManager = function() {" +
				"	this.setFocusListener = function(){};" +
				"};");
	}

	private void createRaphael() throws IOException{
		readString( "var Raphael = function(element, width, height) {" +
				"};");
	}

	private Scriptable getTestLayoutManager() throws IOException{
		Function testData = (Function) scope.get("LayoutManager", scope);
		return testData.construct(context, scope, Context.emptyArgs);
	}
	
}
