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

import java.io.IOException;

import javax.script.ScriptException;

import junit.framework.Assert;

import org.eclipse.virgo.apps.admin.web.stubs.moo.Element;
import org.junit.BeforeClass;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
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
	public void testPageinit() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/dumps.js");
		invokePageInit();
		
		//assertTrue("Page ready has not been called", this.commonUtil.isPageReady());
	}
	
}
