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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.management.console.stubs.objects.Dollar;
import org.eclipse.virgo.management.console.stubs.types.Element;
import org.junit.Before;
import org.junit.Test;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 *
 */
public class ArtifactsJSTests extends AbstractJSTests {

    @Before
    public void setUpArtifactsJS() throws ScriptException, IOException, NoSuchMethodException {
        addCommonObjects();
        readFile("src/main/webapp/js/artifacts.js");

        invokePageInit();
        assertNotNull(commonUtil.getLastQueryCallBack());

        commonUtil.getLastQueryCallBack().call(context, scope, scope, new Object[] { this.getTestData(), "type" });
        assertTrue("Page ready has not been called", commonUtil.isPageReady());
    }

    @Test
    public void testTreeLoad() {
        Dollar.getEachOperation().call(context, scope, scope, new Object[] { 1, "filterMatch" });
        assertEquals("Tree inserted in to the wrong dom node", "#artifacts-tree", Dollar.getDollarLookup());
    }

    @Test
    public void testFileUpload() throws IOException {
        ScriptableObject uploadManager = (ScriptableObject) scope.get("uploadManager", scope);

        Element element = (Element) ((Function) scope.get("Element", scope)).construct(context, scope, new Object[] { "<div />" });
        Dollar.setDollarLookupResultForIds(element, 3);
        ScriptableObject.callMethod(uploadManager, "toggle", new Object[] {});
        assertTrue(element.getClasses().contains("button-selected"));

        Element element2 = (Element) ((Function) scope.get("Element", scope)).construct(context, scope, new Object[] { "#upload-form" });
        Dollar.setDollarLookupResultForIds(element2, 1);
        assertFalse((Boolean) Context.jsToJava(ScriptableObject.getProperty(uploadManager, "uploading"), Boolean.class));
        ScriptableObject.callMethod(uploadManager, "startUpload", new Object[] {});
        assertTrue((Boolean) Context.jsToJava(ScriptableObject.getProperty(uploadManager, "uploading"), Boolean.class));
        assertTrue(element2.isSubmitted());

        Element element3 = (Element) ((Function) scope.get("Element", scope)).construct(context, scope, new Object[] { "#upload-target-id" });
        Element element4 = (Element) ((Function) scope.get("Element", scope)).construct(context, scope, new Object[] { "#uploadLocations" });
        element3.setContentDocument(element4);
        Dollar.setDollarLookupResultForIds(new Scriptable[] { element3 }, 1);
        ScriptableObject.callMethod(uploadManager, "deployComplete", new Object[] {});
        assertFalse((Boolean) Context.jsToJava(ScriptableObject.getProperty(uploadManager, "uploading"), Boolean.class));
        assertTrue(alertMsg.startsWith("Deployment result"));

    }

    @Test
    public void testTreeTopLevelTwisty() throws IOException {
        ScriptableObject tree = (ScriptableObject) scope.get("tree", scope);
        ScriptableObject.callMethod(tree, "nodeTwistyClicked", new Object[] { getTestTopLevelEventData() });
        assertEquals("search/org.eclipse.virgo.kernel:type=ArtifactModel,*", commonUtil.getLastQuery());
    }

    @Test
    public void testTreeTwisty() throws IOException {
        ScriptableObject tree = (ScriptableObject) scope.get("tree", scope);
        ScriptableObject.callMethod(tree, "nodeTwistyClicked", new Object[] { getTestOtherLevelEventData() });
        assertEquals("read/objectName", commonUtil.getLastQuery());
    }

    private Scriptable getTestData() throws IOException {
		readString( "var TestData = function() {" +
					"	this.value = {};" +
					"};");
        Function testData = (Function) scope.get("TestData", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }

    private Scriptable getTestTopLevelEventData() throws IOException {
		readString( "var TestEventData = function() {" +
					"	this.data = {};" +
					"	this.data.node = new Element('node');" +
					"   this.data.node.addClass('top-level');" +
					"	this.data.queryData = {};" +
					"};");
        Function testData = (Function) scope.get("TestEventData", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }

    private Scriptable getTestOtherLevelEventData() throws IOException {
		readString( "var TestEventData = function() {" +
					"	this.data = {};" +
					"	this.data.node = new Element('node');" +
					"	this.data.queryData = {};" +
					"	this.data.queryData.toString = 'objectName';" +
					"};");
        Function testData = (Function) scope.get("TestEventData", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }
}
