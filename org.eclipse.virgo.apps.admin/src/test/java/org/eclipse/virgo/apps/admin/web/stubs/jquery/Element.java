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

package org.eclipse.virgo.apps.admin.web.stubs.jquery;

import java.util.ArrayList;
import java.util.List;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 */
public class Element extends ParentStub {
    
    private static final long serialVersionUID = 1L;


    private static List<String> CONSTRUCTOR_ARGUMENT_TRACE = new ArrayList<String>();

    private static Element LAST_REPLACEMENT;
    
    private final String constructorArgument;
    
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
    
    public ScriptableObject jsFunction_replaceWith(ScriptableObject replacement){
        LAST_REPLACEMENT = ((Element) Context.jsToJava(replacement, Element.class));
        return this;
    }
    
    // Test methods
    
   public String getConstructorArgument() {
       return this.constructorArgument;
   }
    
   public static List<String> getConstructorArgumentTrace() {
       return CONSTRUCTOR_ARGUMENT_TRACE;
   }
   
   public static Element getLastReplacement() {
       return LAST_REPLACEMENT;
   }

}
