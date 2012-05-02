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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import junit.framework.Assert;

import org.eclipse.virgo.apps.admin.web.stubs.moo.Element;
import org.eclipse.virgo.apps.admin.web.stubs.moo.Request;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 *
 *
 */
public class ArtifactsJSTests extends AbstractJSTests {
	
	
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
	public void testPageinit() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/artifacts.js");
		
		invokePageInit();
		assertNotNull(Request.getLastSentOnSuccess());
		
		readFile("src/test/resources/ArtifactData.js");
		Function jsonData = (Function) SCOPE.get("Data", SCOPE);
		Request.getLastSentOnSuccess().call(CONTEXT, SCOPE, SCOPE, new Object[]{jsonData.construct(CONTEXT, SCOPE, Context.emptyArgs), "type"});
		assertTrue("Page ready has not been called", this.commonUtil.isPageReady());
	}
	
	@Test
	public void testTreeLoad() {
		assertEquals("Tree inserted in to the wrong dom node", dollarLookupToReturn, Element.getInjectedInto());
	}
	
	@Test
	public void testFileUpload() {
		setup();
		ScriptableObject uploadManager = (ScriptableObject) SCOPE.get("uploadManager", SCOPE);
		ScriptableObject.callMethod(uploadManager, "toggle", new Object[0]);
		Element dollar = (Element) Context.jsToJava(dollarLookupToReturn, Element.class);
		assertTrue(dollar.getClassNames().contains("button-selected"));

		assertFalse((Boolean) Context.jsToJava(ScriptableObject.getProperty(uploadManager, "uploading"), Boolean.class));
		ScriptableObject.callMethod(uploadManager, "startUpload", new Object[0]);
		assertTrue(dollar.isSubmitted());
		assertTrue((Boolean) Context.jsToJava(ScriptableObject.getProperty(uploadManager, "uploading"), Boolean.class));
		ScriptableObject.callMethod(uploadManager, "uploadComplete", new Object[0]);
		assertFalse((Boolean) Context.jsToJava(ScriptableObject.getProperty(uploadManager, "uploading"), Boolean.class));
	}

	@Test
	public void testTreeTopLevelTwisty() {
		setup();
		ScriptableObject tree = (ScriptableObject) SCOPE.get("tree", SCOPE);
		ScriptableObject.callMethod(tree, "renderTopLevel", new Object[]{"testObjectName", "testParent"});
		assertEquals("testParent", dollarLookup);
		assertEquals("hostPrefix/jolokia/search/org.eclipse.virgo.kernel:type=ArtifactModel,*", Request.getLastSentUrl());
	}
	
	@Test
	public void testTreeTwisty() {
		setup();
		ScriptableObject tree = (ScriptableObject) SCOPE.get("tree", SCOPE);
		ScriptableObject.callMethod(tree, "renderArtifact", new Object[]{"testObjectName", "testParent"});
		assertEquals("testParent", dollarLookup);
		assertEquals("hostPrefix/jolokia/read/testObjectName", Request.getLastSentUrl());
	}
	
}
