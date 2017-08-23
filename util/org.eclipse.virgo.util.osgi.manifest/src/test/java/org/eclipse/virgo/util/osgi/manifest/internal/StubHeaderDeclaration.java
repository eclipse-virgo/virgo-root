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

package org.eclipse.virgo.util.osgi.manifest.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;


public class StubHeaderDeclaration implements HeaderDeclaration {
    
    private Map<String, String> attributes;
    
    private Map<String, String> directives;
    
    private List<String> names;
    
    StubHeaderDeclaration(String... names) {
        this(new HashMap<String, String>(), new HashMap<String, String>(), names);
    }
    
    StubHeaderDeclaration(Map<String, String> attributes, Map<String, String> directives, String... names) {
        this.attributes = attributes;
        
        this.directives = directives;
        
        this.names = Arrays.asList(names);
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public Map<String, String> getDirectives() {
        return this.directives;
    }

    public List<String> getNames() {
        return this.names;
    }

}
