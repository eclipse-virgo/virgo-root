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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import junit.framework.Assert;

import org.eclipse.virgo.apps.admin.web.stubs.moo.Element;
import org.eclipse.virgo.apps.admin.web.stubs.moo.Request;
import org.junit.BeforeClass;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 *
 */
public class DumpsJSTests extends AbstractJSTests {
	
	@BeforeClass
	public static void setup(){
		Function fObj = (Function) SCOPE.get(Element.class.getSimpleName(), SCOPE);
		if (fObj instanceof Function) {
		    Function constructor = (Function)fObj;
			dollarLookupToReturn = (ScriptableObject) constructor.construct(Context.getCurrentContext(), constructor.getParentScope(), new Object[]{"testElement"});
		} else {
			Assert.fail("Element constructor not found");
		}
	}
	
	@Test
	public void testSetLocationString() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/dumps.js");
		invokePageInit();
		
		readFile("src/test/resources/DumpData.js");
		Function jsonData = (Function) SCOPE.get("Location", SCOPE);
		Scriptable construct = jsonData.construct(CONTEXT, SCOPE, Context.emptyArgs);
		Request.onSuccess.call(CONTEXT, SCOPE, SCOPE, new Object[]{construct});
		
		Element dollar = (Element) Context.jsToJava(dollarLookupToReturn, Element.class);
		assertEquals("Location: Testing", dollar.getAppendedText());
	}

	@Test
	public void testDisplayDumps() throws IOException {
		addCommonObjects();
		ScriptableObject dumpViewer = (ScriptableObject) ScriptableObject.getProperty(SCOPE, "dumpViewer");
		ScriptableObject.callMethod(dumpViewer, "displayDumps", new Object[]{});

		Function jsonData = (Function) SCOPE.get("DataOne", SCOPE);
		Scriptable construct = jsonData.construct(CONTEXT, dumpViewer, Context.emptyArgs);
		Request.onSuccess.call(CONTEXT, SCOPE, dumpViewer, new Object[]{construct});
		
		assertEquals(dollarLookupToReturn, Element.getInjectedInto());
		assertEquals("dump-item-content", dollarLookup);
		assertTrue("Page ready has not been called", this.commonUtil.isPageReady());
	}
	
	@Test
	public void testDisplayDumpEntries(){
		ScriptableObject dumpViewer = (ScriptableObject) ScriptableObject.getProperty(SCOPE, "dumpViewer");
		ScriptableObject.callMethod(dumpViewer, "displayDumpEntries", new Object[]{"testId"});

		Function jsonData = (Function) SCOPE.get("DataTwo", SCOPE);
		Scriptable construct = jsonData.construct(CONTEXT, SCOPE, Context.emptyArgs);
		Request.onSuccess.call(CONTEXT, SCOPE, dumpViewer, new Object[]{construct});
		
		assertEquals(dollarLookupToReturn, Element.getInjectedInto());
		assertEquals("dump-items", dollarLookup);
	}
	
	@Test
	public void testDisplayDumpEntry(){
		ScriptableObject dumpViewer = (ScriptableObject) ScriptableObject.getProperty(SCOPE, "dumpViewer");
		ScriptableObject.callMethod(dumpViewer, "displayDumpEntry", new Object[]{"testId", "testQueryString"});

		Function jsonData = (Function) SCOPE.get("DataOne", SCOPE);
		Scriptable construct = jsonData.construct(CONTEXT, SCOPE, Context.emptyArgs);
		Request.onSuccess.call(CONTEXT, SCOPE, dumpViewer, new Object[]{construct});
		
		assertEquals(dollarLookupToReturn, Element.getInjectedInto());
		assertEquals("dump-item-content", dollarLookup);
	}
	
}
