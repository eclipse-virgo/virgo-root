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

import org.eclipse.virgo.apps.admin.web.stubs.common.Server;
import org.eclipse.virgo.apps.admin.web.stubs.jquery.Element;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Function;

/**
 * Unit test of overview.js.
 */
public class OverviewJSTests extends AbstractJSTests {
    
    private static String[] EXPECTED_HEADERS = {"Name", "Value"};
	
	@Test
	public void testPageinit() throws ScriptException, NoSuchMethodException, IOException{
		addCommonObjects();
		readFile("src/main/webapp/js/overview.js");

		invokePageInit();
		
		Function callback = Server.getCallbackFunction();
		Object[] args = new Object[]{new String[]{"TestRowData"}};
		callback.call(CONTEXT, SCOPE, SCOPE, args);
		
		assertEquals("Wrong DOM node replaced", "#server-overview", Element.getLastReplacedNodeConstructorArgument());
		assertArrayEquals("Wrong table headers", EXPECTED_HEADERS, this.commonUtil.getLastMakeTableHeaders());
		assertEquals("Wrong table rows", args[0], this.commonUtil.getLastMakeTableRows());
		
		assertTrue("Page ready has not been called", this.commonUtil.isPageReady());
	}
	
}
