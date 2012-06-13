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

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

/**
 *
 *
 */
public class BundlesGuiJSTests extends AbstractJSTests {
	
	@Test
	public void testConstructors() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/bundlesGui.js");

		((Function) scope.get("LayoutManager", scope)).construct(context, scope, new Object[]{null, null, null});
		((Function) scope.get("Bundle", scope)).construct(context, scope, new Object[]{getTestPaper(), getTestRawBundle(), null});
		((Function) scope.get("Relationship", scope)).construct(context, scope, new Object[]{getTestPaper(), getTestRawBundle(), getTestRawBundle(), null});
		((Function) scope.get("InfoBox", scope)).construct(context, scope, new Object[]{});

	}

	private Scriptable getTestRawBundle() throws IOException{
		readString( "var TestRawBundle = function() {" +
					"	this.Identifier = 5;" +
					"	this.summary = 'summary';" +
					"	this.ExportedPackages = [1,2];" +
					"	this.ImportedPackages = [3,4];" +
					"};");
		Function testData = (Function) scope.get("TestRawBundle", scope);
		return testData.construct(context, scope, Context.emptyArgs);
	}

	private Scriptable getTestPaper() throws IOException{
		readString( "var TestPaper = function() {" +
				"	this.text = function(){return new TestElement();};" +
				"	this.path = function(){return new TestElement();};" +
				"	this.circle = function(){return new TestElement();};" +
				"	this.rect = function(){return new TestElement();};" +
					"};" + 
				"	var TestElement = function() {" +
				"	this.attr = function(){return new TestElement();};" +
				"	this.hide = function(){return new TestElement();};" +
				"	this.getBBox = function(){return new TestElement();};" +
				"	this.toBack = function(){return new TestElement();};" +
				"	this.dblclick = function(){return new TestElement();};" +
				"	this.click = function(){return new TestElement();};" +
				"	this.width = 5;" +
				"	this.height = 5;" +
					"};");
		Function testData = (Function) scope.get("TestPaper", scope);
		return testData.construct(context, scope, Context.emptyArgs);
	}
	
}
