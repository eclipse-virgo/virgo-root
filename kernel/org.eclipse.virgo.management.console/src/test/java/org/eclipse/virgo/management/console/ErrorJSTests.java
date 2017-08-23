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

import java.io.IOException;

import javax.script.ScriptException;

import org.junit.Test;

/**
 *
 *
 */
public class ErrorJSTests extends AbstractJSTests {
	
	@Test
	public void testParse() throws ScriptException, IOException{
		readFile("src/main/webapp/js/error.js");
	}
	
}
