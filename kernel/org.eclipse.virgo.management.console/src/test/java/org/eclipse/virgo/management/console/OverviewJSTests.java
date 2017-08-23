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
package org.eclipse.virgo.management.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.management.console.stubs.objects.Dollar;
import org.junit.Test;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Unit test of overview.js.
 */
public class OverviewJSTests extends AbstractJSTests {
	
	@Test
	public void testPageinit() throws ScriptException, NoSuchMethodException, IOException{
		addCommonObjects();
		readFile("src/main/webapp/js/overview.js");

		invokePageInit();
		
		assertEquals("hostPrefix/jolokia/version", Dollar.getAjaxUrl());
		
		Dollar.getAjaxSuccess().call(context, scope, scope, new Object[] { getTestVersion() });
		
		assertTrue("Page ready has not been called", commonUtil.isPageReady());
	}

    private Scriptable getTestVersion() throws IOException {

        readString("var Data = function() {" + 
            "   this.value = {info: {vendor: 'a', product: 'b', version: '1'}};" +
            "};");

        Function testData = (Function) scope.get("Data", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }	
}
