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

import org.eclipse.virgo.apps.admin.web.stubs.objects.Dollar;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

/**
 * All the methods in this test class should be run in the defined order. In particular, testPageInit must be run first
 * to perform common setup.
 */
public class RepositoriesJSTests extends AbstractJSTests {

    @Test
    public void testPageinit() throws ScriptException, IOException, NoSuchMethodException {
        // The next two lines are common setup that will be used by other test methods.
        addCommonObjects();
        readFile("src/main/webapp/js/repositories.js");

        invokePageInit();

        assertNotNull(Dollar.getAjaxSuccess());
        Dollar.getAjaxSuccess().call(CONTEXT, SCOPE, SCOPE, new Object[] { getTestData() });
        
        Function eachOperation = Dollar.getEachOperation();
        eachOperation.call(CONTEXT, SCOPE, SCOPE, new Object[] {0, "org.eclipse.virgo.kernel:name=usr,type=Repository"});

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
        Object[] args = new Object[]{};
        Scriptable event = eventConstructor.construct(Context.getCurrentContext(), SCOPE, args);
        
        displayFunction.call(CONTEXT, SCOPE, repositories, new Object[]{event});
        
        assertNotNull(Dollar.getAjaxSuccess());
        Dollar.getAjaxSuccess().call(CONTEXT, SCOPE, SCOPE, new Object[] { getDisplayTestData() });

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
