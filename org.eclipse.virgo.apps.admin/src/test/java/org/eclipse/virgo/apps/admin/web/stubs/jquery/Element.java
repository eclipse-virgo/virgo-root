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

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.ScriptableObject;

/**
 */
public class Element extends ParentStub {
    
    private String creationArgument;

    /**
     * Prototype constructor
     */
    public Element() {
    }
    
    /**
     * JavaScript Constructor
     */
    public Element(ScriptableObject name) {
        creationArgument = ((String) Context.jsToJava(name, String.class));
    }
    
    public ScriptableObject jsFunction_replaceWith(ScriptableObject replacement){
        return this;
    }
    


}
