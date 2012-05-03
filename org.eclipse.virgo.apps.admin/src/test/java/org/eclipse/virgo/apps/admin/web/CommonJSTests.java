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

import org.junit.Test;

import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 *
 *
 */
public class CommonJSTests extends AbstractJSTests {
	
	@Test
	public void testParse() throws ScriptException, IOException{
		Scriptable document = ((Function) scope.get("Element", scope)).construct(context, scope, new Object[]{"<div />"});
		ScriptableObject.putProperty(scope, "document", document);
		
		readFile("src/main/webapp/js/common.js");

	}
	
}
