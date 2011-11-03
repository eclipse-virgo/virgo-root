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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.apps.admin.web.stubs.common.Server;
import org.eclipse.virgo.apps.admin.web.stubs.moo.HtmlTable;
import org.junit.Test;
import sun.org.mozilla.javascript.internal.Function;

/**
 *
 *
 */
public class OverviewJSTests extends AbstractJSTests {
	
	/**
	 * Test that the init script for the page reports back that the page is ready for display
	 * 
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 * @throws IOException
	 */
	@Test
	public void testPageinit() throws ScriptException, NoSuchMethodException, IOException{
		addCommonObjects();
		readFile("src/main/webapp/js/overview.js");

		invokePageInit();
		
		Function callback = Server.getCallbackFunction();
		Object[] args = new Object[]{new String[]{"TestRowData"}};
		callback.call(CONTEXT, SCOPE, SCOPE, args);
		
		assertTrue("Wrong dom node replacement, got: " + HtmlTable.REPLACED_NODE_NAME, "server-overview".equals(HtmlTable.REPLACED_NODE_NAME));
		assertTrue("Overview row data lost", HtmlTable.row_data.equals(args[0]));

		assertTrue("Page ready has not been called", this.commonUtil.isPageReady());
		
	}
	
}
