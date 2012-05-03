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

package org.eclipse.virgo.apps.admin.web.stubs.objects;

import java.util.HashMap;
import java.util.Map;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 * 
 */
public class Dollar {
	
	private static Context CONTEXT; 
	
	private static ScriptableObject SCOPE;
	
	private static String dollarLookup = "";
	
	private static Function ajax_success;

	private static String ajax_url;
	
	private static Map<String, Function> ajaxSuccessByUrl = new HashMap<String, Function>();

	private static Scriptable each_array;

	private static Function each_operation;

    private static Scriptable lookupResultForIds;

    private Dollar(Context context, ScriptableObject scope) {
    }
    
    public static void init(Context context, ScriptableObject scope) {
    	CONTEXT = context;
    	SCOPE = scope;
    }

    // JavaScript Functions
    
    public static Object dollar(ScriptableObject name, Scriptable xxx) {
        return dollar(name); // ignore xxx for now
    }

	public static Object dollar(ScriptableObject name){
		Dollar.dollarLookup = (String) Context.jsToJava(name, String.class);
		if (Dollar.dollarLookup.startsWith("#") && Dollar.lookupResultForIds != null) {
		    Scriptable result = Dollar.lookupResultForIds;
		    Dollar.lookupResultForIds = null;
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
	    Dollar.lookupResultForIds = dollarLookupResultForIds;
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
