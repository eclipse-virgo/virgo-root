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


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

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
public class DumpsJSTests extends AbstractJSTests {
    
    private static final String DUMPDIR_URL = "hostPrefix/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/ConfiguredDumpDirectory";
    
    private static final String DUMPS_URL = "hostPrefix/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/Dumps";
	
    @Test
    public void testPageinit() throws ScriptException, IOException, NoSuchMethodException {

        // Common setup that will be used by other test methods.
        readFile("src/main/webapp/js/dumps.js");

        addCommonObjects();

        invokePageInit();
        
        Map<String, Function> successByUrl = Dollar.getAndClearAjaxSuccessByUrl();
        
        assertTrue(successByUrl.containsKey(DUMPDIR_URL));
        successByUrl.get(DUMPS_URL).call(CONTEXT, SCOPE, SCOPE, new Object[] { getTestDumpDirectory() });
        
        assertTrue(successByUrl.containsKey(DUMPS_URL));
        successByUrl.get(DUMPS_URL).call(CONTEXT, SCOPE, SCOPE, new Object[] { getTestDumpList() });

        assertTrue("Page ready has not been called", commonUtil.isPageReady());
    }
    
    private Scriptable getTestDumpDirectory() throws IOException {

        readString("var Data = function() {" + //
            "   this.value = ['servicability/dump'];" + //
            "   this.value.Properties = {};" + //
            "};");

        Function testData = (Function) SCOPE.get("Data", SCOPE);
        return testData.construct(CONTEXT, SCOPE, Context.emptyArgs);
    }

    private Scriptable getTestDumpList() throws IOException {

        readString("var Data = function() {" + //
            "   this.value = ['2012-05-02-13-30-643'];" + //
            "   this.value.Properties = {};" + //
            "};");

        Function testData = (Function) SCOPE.get("Data", SCOPE);
        return testData.construct(CONTEXT, SCOPE, Context.emptyArgs);
    }

}
