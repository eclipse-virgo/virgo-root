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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.apps.admin.web.stubs.objects.Dollar;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * All the methods in this test class should be run in the defined order. In particular, testPageInit must be run first
 * to perform common setup.
 */
public class RepositoriesJSTests extends AbstractJSTests {

    @Test
    public void testPageinit() throws ScriptException, IOException, NoSuchMethodException {

        // Common setup that will be used by other test methods.
        readFile("src/main/webapp/js/repositories.js");

        addCommonObjects();

        invokePageInit();

        assertNotNull(Dollar.getAjaxSuccess());
        Dollar.getAjaxSuccess().call(CONTEXT, SCOPE, SCOPE, new Object[] { getTestData() });

        Function eachOperation = Dollar.getEachOperation();
        eachOperation.call(CONTEXT, SCOPE, SCOPE, new Object[] { 0, "org.eclipse.virgo.kernel:name=usr,type=Repository" });

        assertTrue("Page ready has not been called", this.commonUtil.isPageReady());

    }

    private Scriptable getTestData() throws IOException {

        readString("var Data = function() {" + //
            "   this.value = ['org.eclipse.virgo.kernel:name=usr,type=Repository', 'org.eclipse.virgo.kernel:name=ext,type=Repository'];" + //
            "   this.value.Properties = {};" + //
            "};");

        Function testData = (Function) SCOPE.get("Data", SCOPE);
        return testData.construct(CONTEXT, SCOPE, Context.emptyArgs);
    }

    @Test
    public void testDisplay() throws Exception {
        Scriptable repositories = (Scriptable) SCOPE.get("Repositories", SCOPE);

        Function displayFunction = (Function) repositories.get("display", SCOPE);

        readString("var ClickEvent = function() {" + //
            "   this.data = new Element('<div />');" + //
            "   this.mbeanName = 'mbeanName';" + //
            "};");

        Function eventConstructor = (Function) SCOPE.get("ClickEvent", SCOPE);
        Object[] args = new Object[] {};
        Scriptable event = eventConstructor.construct(Context.getCurrentContext(), SCOPE, args);

        displayFunction.call(CONTEXT, SCOPE, repositories, new Object[] { event });

        assertNotNull(Dollar.getAjaxSuccess());
        Dollar.getAjaxSuccess().call(CONTEXT, SCOPE, SCOPE, new Object[] { getDisplayTestData() });

    }

    @Test
    public void testDeploy() throws Exception {
        addCommonObjects();

        Scriptable repositories = (Scriptable) SCOPE.get("Repositories", SCOPE);

        Function deployFunction = (Function) repositories.get("deploy", SCOPE);

        deployFunction.call(CONTEXT, SCOPE, repositories, new Object[] { "anArtifact" });

        Scriptable lastBulkQuery = this.commonUtil.getLastBulkQuery();
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
        Scriptable aResponse = (Scriptable) SCOPE.get("aResponse", SCOPE);
        this.commonUtil.getLastBulkQueryCallBack().call(CONTEXT, SCOPE, SCOPE, new Object[] { aResponse });
        
        readString("var console = { log : function(xmlHttpRequest, textStatus, errorThrown) {}};");
        this.commonUtil.getLastBulkQueryErrorCallBack().call(CONTEXT, SCOPE, SCOPE, new Object[] { "xmlHttpRequest", "textStatus", "errorThrown" });
    }

    private Scriptable getDisplayTestData() throws IOException {

        readString("var Data = function() {" + //
            "   this.value = {AllArtifactDescriptorSummaries : []};" + //
            "   this.value.Properties = {};" + //
            "};");

        Function testData = (Function) SCOPE.get("Data", SCOPE);
        return testData.construct(CONTEXT, SCOPE, Context.emptyArgs);
    }

}
