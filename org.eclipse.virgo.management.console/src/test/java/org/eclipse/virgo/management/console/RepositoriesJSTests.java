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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.management.console.stubs.objects.Dollar;
import org.junit.Before;
import org.junit.Test;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * All the methods in this test class should be run in the defined order. In particular, testPageInit must be run first
 * to perform common setup.
 */
public class RepositoriesJSTests extends AbstractJSTests {

    @Before
    public void setUpRepositoriesJS() throws ScriptException, IOException, NoSuchMethodException {

        // Common setup that will be used by other test methods.
        readFile("src/main/webapp/js/repositories.js");

        addCommonObjects();

        invokePageInit();
    }

    @Test
    public void testPageInit() throws IOException {
        assertNotNull(Dollar.getAjaxSuccess());
        Dollar.getAjaxSuccess().call(context, scope, scope, new Object[] { getTestData() });

        Function eachOperation = Dollar.getEachOperation();
        eachOperation.call(context, scope, scope, new Object[] { 0, "org.eclipse.virgo.kernel:name=usr,type=Repository" });

        assertTrue("Page ready has not been called", commonUtil.isPageReady());

    }

    private Scriptable getTestData() throws IOException {

        readString("var Data = function() {" + //
            "   this.value = ['org.eclipse.virgo.kernel:name=usr,type=Repository', 'org.eclipse.virgo.kernel:name=ext,type=Repository'];" + //
            "   this.value.Properties = {};" + //
            "};");

        Function testData = (Function) scope.get("Data", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }

    @Test
    public void testDisplay() throws Exception {
        Scriptable repositories = (Scriptable) scope.get("Repositories", scope);

        Function displayFunction = (Function) repositories.get("display", scope);

        readString("var ClickEvent = function() {" + //
            "   this.data = new Element('<div />');" + //
            "   this.mbeanName = 'mbeanName';" + //
            "};");

        Function eventConstructor = (Function) scope.get("ClickEvent", scope);
        Object[] args = new Object[] {};
        Scriptable event = eventConstructor.construct(Context.getCurrentContext(), scope, args);

        displayFunction.call(context, scope, repositories, new Object[] { event });

        assertNotNull(Dollar.getAjaxSuccess());
        Dollar.getAjaxSuccess().call(context, scope, scope, new Object[] { getDisplayTestData() });

    }

    @Test
    public void testDeploy() throws Exception {
        Scriptable repositories = (Scriptable) scope.get("Repositories", scope);

        Function deployFunction = (Function) repositories.get("deploy", scope);

        deployFunction.call(context, scope, repositories, new Object[] { "anArtifact" });

        Scriptable lastBulkQuery = commonUtil.getLastBulkQuery();
        Scriptable[] lastBulkQueryArray = (Scriptable[])Context.jsToJava(lastBulkQuery, Scriptable[].class);
        Scriptable argumentsProperty = (Scriptable)ScriptableObject.getProperty(lastBulkQueryArray[0], "arguments");
        String[] arguments = (String[])Context.jsToJava(argumentsProperty, String[].class);
        assertEquals(1, arguments.length);
        assertEquals("anArtifact", arguments[0]);

        readString("var aResponse = [{" + //
            "   value : {type : 'bundle'," + //
            "       symbolicName : 'someBundle'," + //
            "       version : '2.1'" + //
            "   }" + //
            "}];");
        Scriptable aResponse = (Scriptable) scope.get("aResponse", scope);
        commonUtil.getLastBulkQueryCallBack().call(context, scope, scope, new Object[] { aResponse });
        
        readString("var console = { log : function(xmlHttpRequest, textStatus, errorThrown) {}};");
        commonUtil.getLastBulkQueryErrorCallBack().call(context, scope, scope, new Object[] { "xmlHttpRequest", "textStatus", "errorThrown" });
    }

    private Scriptable getDisplayTestData() throws IOException {

        readString("var Data = function() {" + //
            "   this.value = {AllArtifactDescriptorSummaries : []};" + //
            "   this.value.Properties = {};" + //
            "};");

        Function testData = (Function) scope.get("Data", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }

}
