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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import javax.script.ScriptException;

import org.eclipse.virgo.management.console.stubs.objects.Dollar;
import org.eclipse.virgo.management.console.stubs.types.Element;
import org.junit.Before;
import org.junit.Test;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * All the methods in this test class should be run in the defined order. In particular, testPageInit must be run first
 * to perform common setup.
 */
public class DumpsJSTests extends AbstractJSTests {
    
    private static final String DUMPDIR_URL = "hostPrefix/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/ConfiguredDumpDirectory";
    
    private static final String DUMPS_URL = "hostPrefix/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/Dumps";
	
    @Before
    public void setUpDumpsJS() throws ScriptException, IOException, NoSuchMethodException {

        // Common setup that will be used by other test methods.
        readFile("src/main/webapp/js/dumps.js");

        addCommonObjects();

        invokePageInit();
    }

    @Test
    public void testPageInit() throws IOException {
        Map<String, Function> successByUrl = Dollar.getAndClearAjaxSuccessByUrl();
        
        assertTrue(successByUrl.containsKey(DUMPDIR_URL));
        successByUrl.get(DUMPS_URL).call(context, scope, scope, new Object[] { getTestDumpDirectory() });
        
        assertTrue(successByUrl.containsKey(DUMPS_URL));
        successByUrl.get(DUMPS_URL).call(context, scope, scope, new Object[] { getTestDumpList() });

        assertTrue("Page ready has not been called", commonUtil.isPageReady());
    }
    
    private Scriptable getTestDumpDirectory() throws IOException {

        readString("var Data = function() {" + //
            "   this.value = ['servicability/dump'];" + //
            "   this.value.Properties = {};" + //
            "};");

        Function testData = (Function) scope.get("Data", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }

    private Scriptable getTestDumpList() throws IOException {

        readString("var Data = function() {" + //
            "   this.value = ['2012-05-02-13-30-643'];" + //
            "   this.value.Properties = {};" + //
            "};");

        Function testData = (Function) scope.get("Data", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }
    
    @Test
    public void testDisplayDumpEntries() throws ScriptException, IOException, NoSuchMethodException {
        
        Function dumpViewerConstructor = (Function) scope.get("DumpViewer", scope);        
        Scriptable dumpViewer = dumpViewerConstructor.construct(context, scope, new Object[]{});
        
        readString( "var DumpEntryList = function() {" +
            "   this.children = function() {return [];};" +
            "};");
        Function dumpEntryList = (Function) scope.get("DumpEntryList", scope);
        Scriptable listElement = dumpEntryList.construct(context, scope, Context.emptyArgs);
        Dollar.setDollarLookupResultForIds(listElement);

        readString("var ClickEvent = function() {" + //
            "   this.data = new Element('<div />');" + //
            "};");
        
        Function eventConstructor = (Function) scope.get("ClickEvent", scope);
        Object[] args = new Object[] {};
        Scriptable event = eventConstructor.construct(Context.getCurrentContext(), scope, args);
        Function displayDumpEntriesFunction = (Function) dumpViewer.get("displayDumpEntries", scope);
        displayDumpEntriesFunction.call(context, scope, dumpViewer, new Object[] {event});

        Function displaySelectedDumpEntriesFunction = (Function) dumpViewer.get("displaySelectedDump", scope);
        displaySelectedDumpEntriesFunction.call(context, scope, dumpViewer, new Object[] {});
    }
    
    @Test
    public void testDisplaySelectedDumpResponse() throws ScriptException, IOException, NoSuchMethodException {
        
        Function dumpViewerConstructor = (Function) scope.get("DumpViewer", scope);        
        Scriptable dumpViewer = dumpViewerConstructor.construct(context, scope, new Object[]{});
        
        Object[][] json = new Object[][]{{"a","b"},{"c","d"}};

        readString("var DumpListItem = function() {" + //
            "   this.attr = function(id){return 'anId'};" + //
            "};");
        
        Function dumpListItemConstructor = (Function) scope.get("DumpListItem", scope);
        Object[] args = new Object[] {};
        Scriptable dumpListItem = dumpListItemConstructor.construct(Context.getCurrentContext(), scope, args);

        Function displaySelectedDumpEntriesResponseFunction = (Function) dumpViewer.get("displaySelectedDumpResponse", scope);
        displaySelectedDumpEntriesResponseFunction.call(context, scope, dumpViewer, new Object[] {Context.javaToJS(json, scope), dumpListItem});
        
        readString( "var DumpEntryList = function() {" +
            "   this.children = function() {return [];};" +
            "   this.append = function(dumpEntryListItem) {return this;};" +
            "};");
        Function dumpEntryList = (Function) scope.get("DumpEntryList", scope);
        Scriptable listElement = dumpEntryList.construct(context, scope, Context.emptyArgs);
        Dollar.setDollarLookupResultForIds(listElement, 2);
        
        Element.resetClicked();
        Function eachOperation = Dollar.getEachOperation();
        eachOperation.call(context, scope, eachOperation, new Object[]{Context.javaToJS(1, scope), new Object[]{"bar.txt","foo"}});
        assertEquals(1, Element.isClicked());
        eachOperation.call(context, scope, eachOperation, new Object[]{Context.javaToJS(1, scope), new Object[]{"summary.txt","foo"}});
        assertEquals(3, Element.isClicked());
    }
    
    @Test
    public void testCreateDump() throws ScriptException, IOException, NoSuchMethodException {
        Function dumpViewerConstructor = (Function) scope.get("DumpViewer", scope);        
        Scriptable dumpViewer = dumpViewerConstructor.construct(context, scope, new Object[]{});
        Function createDumpFunction = (Function) dumpViewer.get("createDump", scope);
        createDumpFunction.call(context, scope, dumpViewer, new Object[] {});
        
        String ajaxUrl = Dollar.getAjaxUrl();
        assertEquals("hostPrefix/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/createDump", ajaxUrl);
    }
    
    @Test
    public void testDeleteDump() throws ScriptException, IOException, NoSuchMethodException {
        Function dumpViewerConstructor = (Function) scope.get("DumpViewer", scope);        
        Scriptable dumpViewer = dumpViewerConstructor.construct(context, scope, new Object[]{});
        Function deleteDumpFunction = (Function) dumpViewer.get("deleteDump", scope);
        
        readString("var Event = function() {" + //
            "   this.data = {attr : function(id){return 'anId'}};" + //
            "};");
        
        Function eventConstructor = (Function) scope.get("Event", scope);
        Object[] args = new Object[] {};
        Scriptable event = eventConstructor.construct(Context.getCurrentContext(), scope, args);
        
        deleteDumpFunction.call(context, scope, dumpViewer, new Object[] {event});
        
        String ajaxUrl = Dollar.getAjaxUrl();
        assertEquals("hostPrefix/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/deleteDump/anId", ajaxUrl);
    }
}
