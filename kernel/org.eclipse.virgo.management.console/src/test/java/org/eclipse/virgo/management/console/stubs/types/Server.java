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

package org.eclipse.virgo.management.console.stubs.types;

import org.mozilla.javascript.Function;

/**
 * 
 *
 */
public class Server extends ParentStub {

    private static final long serialVersionUID = 1L;

    private Function callbackFunction;

    public Server() {
    }

    // Stub methods

    public void jsFunction_getServerOverview(Function callbackFunction) {
        this.callbackFunction = callbackFunction;
    }

    // Test methods

    public Function getCallbackFunction() {
        return this.callbackFunction;
    }

}
