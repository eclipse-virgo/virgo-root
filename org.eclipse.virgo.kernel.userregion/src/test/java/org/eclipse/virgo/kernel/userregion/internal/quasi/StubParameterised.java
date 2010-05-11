/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import java.util.HashMap;
import java.util.Map;

/**
 */
public abstract class StubParameterised {

    @SuppressWarnings("unchecked")
    Map attributes = new HashMap();

    @SuppressWarnings("unchecked")
    Map directives = new HashMap();

    @SuppressWarnings("unchecked")
    public Map getAttributes() {
        return this.attributes;
    }

    public Object getDirective(String key) {
        return this.directives.get(key);
    }

    @SuppressWarnings("unchecked")
    public Map getDirectives() {
        return this.directives;
    }

}
