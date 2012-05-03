/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.apps.admin.web;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.apps.admin.web.stubs.types.Element;
import org.eclipse.virgo.apps.admin.web.stubs.types.Server;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Function;

/**
 * Unit test of overview.js.
 */
public class OverviewJSTests extends AbstractJSTests {
    
    private static String[] EXPECTED_HEADERS = {"Name", "Value"};
    
    private static String[] TEST_ROW = {"TestName", "TestValue"};
	
	@Test
	public void testPageinit() throws ScriptException, NoSuchMethodException, IOException{
		addCommonObjects();
		readFile("src/main/webapp/js/overview.js");

		invokePageInit();
		
		Function callback = Server.getCallbackFunction();
		callback.call(CONTEXT, SCOPE, SCOPE, new Object[]{TEST_ROW});

		assertTrue("Replaced DOM node not looked up", Element.getConstructorArgumentTrace().contains("#server-overview"));
		assertEquals("Wrong replacement DOM node", commonUtil.getLastMakeTableTable(), Element.getLastReplacement());
		assertArrayEquals("Wrong table headers", EXPECTED_HEADERS, commonUtil.getLastMakeTableHeaders());
		assertArrayEquals("Wrong table rows", TEST_ROW, commonUtil.getLastMakeTableRows());
		
		assertTrue("Page ready has not been called", commonUtil.isPageReady());
	}
	
}
