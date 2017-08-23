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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.script.ScriptException;

import org.junit.Test;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 *
 */
public class BundlesGuiJSTests extends AbstractJSTests {
	
	@Test
	public void testConstructors() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();

		FunctionObject raphaelFunction = new FunctionObject("Raphael", BundlesGuiJSTests.class.getDeclaredMethod("raphael", String.class, int.class, int.class), scope);
		ScriptableObject.putProperty(scope, "Raphael", raphaelFunction);
		
		readFile("src/main/webapp/js/bundlesGui.js");

		((Function) scope.get("LayoutManager", scope)).construct(context, scope, new Object[]{"DrawOnMe", 500, 600, null});
		((Function) scope.get("Bundle", scope)).construct(context, scope, new Object[]{getTestPaper(), getTestRawBundle(), null});
		((Function) scope.get("Relationship", scope)).construct(context, scope, new Object[]{getTestPaper(), getTestRawBundle(), getTestRawBundle(), null});
	}
	
	public static Object raphael(String element, int width, int height){
		assertEquals("DrawOnMe", element);
		assertEquals(500, width);
		assertEquals(600, height);
		return null;
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
