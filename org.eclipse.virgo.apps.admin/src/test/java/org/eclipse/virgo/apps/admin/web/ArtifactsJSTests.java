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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.apps.admin.web.stubs.moo.Request;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;

/**
 *
 *
 */
public class ArtifactsJSTests extends AbstractJSTests {
	
	@Test
	public void testPageinit() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/artifacts.js");
		
		invokePageInit();
		assertNotNull(Request.onSuccess);
		
		readFile("src/test/resources/ArtifactData.js");
		Function jsonData = (Function) SCOPE.get("Data", SCOPE);
		Request.onSuccess.call(CONTEXT, SCOPE, SCOPE, new Object[]{jsonData.construct(CONTEXT, SCOPE, Context.emptyArgs)});
		assertTrue("Page ready has not been called", this.commonUtil.isPageReady());
	}
	
}
