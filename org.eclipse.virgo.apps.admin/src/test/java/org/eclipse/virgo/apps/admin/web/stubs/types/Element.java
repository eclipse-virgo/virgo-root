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

package org.eclipse.virgo.apps.admin.web.stubs.types;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.apps.admin.web.stubs.objects.Util;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 */
public class Element extends ParentStub {
    
    private static final long serialVersionUID = 1L;

    private static List<String> CONSTRUCTOR_ARGUMENT_TRACE = new ArrayList<String>();

    private static Element LAST_REPLACEMENT;

	private static Element LAST_APPENDED;

	private static Function READY;
    
    private final String constructorArgument;
    
    private List<String> CLASSES = new ArrayList<String>();
    
    private Map<String, String> CSS = new HashMap<String, String>();
    
    /**
     * Prototype constructor
     */
    public Element() {
        this.constructorArgument = null;
    }
    
    /**
     * JavaScript Constructor
     */
    public Element(ScriptableObject constructorArgument) {
        this.constructorArgument = ((String) Context.jsToJava(constructorArgument, String.class));
        CONSTRUCTOR_ARGUMENT_TRACE.add(this.constructorArgument);
    }

    public void jsFunction_empty(){
    }

	public void jsFunction_ready(Function readyFunction){
		READY = readyFunction;
	}
    
    public ScriptableObject jsFunction_replaceWith(Element replacement){
        LAST_REPLACEMENT = replacement;
        return this;
    }
    
    public ScriptableObject jsFunction_append(Element toAppend){
        LAST_APPENDED = toAppend;
        return this;
    }

    public ScriptableObject jsFunction_addClass(String newClass){
    	CLASSES.add(newClass);
        return this;
    }

    public void jsFunction_click(){
    }
    
	public boolean jsFunction_hasClass(String className){
		return CLASSES.contains(className);
	}

	public boolean jsFunction_removeClass(String className){
		return CLASSES.remove(className);
	}
	
    public void jsFunction_text(String text){
    }


	public ScriptableObject jsFunction_css(String key, String value){
		CSS.put(key, value);
		return this;
	}
	
	public void jsFunction_slideToggle(int time){
		assertEquals(Util.fxTime, time);
	}
    
    // Test helper methods
    
	public static Function getReadyFunction(){
		return READY;
	}
    
	public static List<String> getConstructorArgumentTrace() {
		return CONSTRUCTOR_ARGUMENT_TRACE;
	}
   
	public static Element getLastReplacement() {
		return LAST_REPLACEMENT;
	}
   
	public static Element getLastAppended() {
		return LAST_APPENDED;
	}

	public String getConstructorArgument() {
		return this.constructorArgument;
	}
}
