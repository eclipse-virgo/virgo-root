/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.management.console.stubs.objects;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 */
public final class Dollar {
	
	private static Context CONTEXT; 
	
	private static ScriptableObject SCOPE;
	
	private static String dollarLookup = "";
	
	private static Function ajax_success;

	private static String ajax_url;
	
	private static Map<String, Function> ajaxSuccessByUrl = new HashMap<String, Function>();

	private static Scriptable each_array;

	private static Function each_operation;

    private static Scriptable lookupResultForIds;

    private static Scriptable[] lookupResultArrayForIds;

    private static int lookupResultForIdsCount;

    private Dollar(Context context, ScriptableObject scope) {
    }
    
    public static void init(Context context, ScriptableObject scope) {
    	Dollar.lookupResultForIds = null;
    	CONTEXT = context;
    	SCOPE = scope;
    }

    // JavaScript Functions
    
	public static Object dollar(ScriptableObject name){
		Dollar.dollarLookup = (String) Context.jsToJava(name, String.class);
		if (Dollar.dollarLookup.startsWith("#") && Dollar.lookupResultForIds != null) {
		    Scriptable result = Dollar.lookupResultForIds;
		    if (--Dollar.lookupResultForIdsCount == 0) {
		        Dollar.lookupResultForIds = null;
		    }
            return result;
		} else if (Dollar.dollarLookup.startsWith("#") && Dollar.lookupResultArrayForIds != null) {
		    Scriptable[] result = Dollar.lookupResultArrayForIds;
            if (--Dollar.lookupResultForIdsCount == 0) {
                Dollar.lookupResultArrayForIds = null;
            }
            return result;
		} else {
		    Function elementConstructor = (Function) SCOPE.get("Element", SCOPE);
		    Object[] args = new Object[]{name};
		    Scriptable constructedElement = elementConstructor.construct(CONTEXT, SCOPE, args);
		    return constructedElement;
		}
	}
	
    public static void ajax(Scriptable options){
    	Dollar.ajax_url = (String) Context.jsToJava(ScriptableObject.getProperty(options, "url"), String.class);
    	Dollar.ajax_success = (Function) Context.jsToJava(ScriptableObject.getProperty(options, "success"), Function.class);
    	Dollar.ajaxSuccessByUrl.put(Dollar.ajax_url, Dollar.ajax_success);
    }

	public static void each(Scriptable array, Function operation){
    	Dollar.each_array = array;
    	Dollar.each_operation = operation;
	}
	
	// Test Helper Methods
	
	public static void setDollarLookupResultForIds(Scriptable dollarLookupResultForIds) {
	    setDollarLookupResultForIds(dollarLookupResultForIds, 1);
	}
	
	public static void setDollarLookupResultForIds(Scriptable dollarLookupResultForIds, int count) {
        Dollar.lookupResultForIds = dollarLookupResultForIds;
        Dollar.lookupResultForIdsCount = count;
    }
	
	public static void setDollarLookupResultForIds(Scriptable[] dollarLookupArrayResultForIds, int count) {
        Dollar.lookupResultArrayForIds = dollarLookupArrayResultForIds;
        Dollar.lookupResultForIdsCount = count;
    }
	
	public static String getDollarLookup() {
		return Dollar.dollarLookup;
	}

	public static Function getAjaxSuccess() {
		return Dollar.ajax_success;
	}
	
	public static Map<String, Function> getAndClearAjaxSuccessByUrl() {
	    Map<String, Function> result = new HashMap<String, Function>(Dollar.ajaxSuccessByUrl);
	    Dollar.ajaxSuccessByUrl.clear();
	    return result;
	}

	public static String getAjaxUrl() {
		return Dollar.ajax_url;
	}

	public static Scriptable getEachArray() {
		return Dollar.each_array;
	}

	public static Function getEachOperation() {
		return Dollar.each_operation;
	}
    
}
