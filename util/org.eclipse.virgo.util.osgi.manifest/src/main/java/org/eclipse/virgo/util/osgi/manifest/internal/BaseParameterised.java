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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.Parameterised;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;


abstract class BaseParameterised implements Parameterised {

    protected String name;

    private final HeaderParser parser;
    
    private Map<String, String> attributes = new HashMap<String, String>();
    
    private Map<String, String> directives = new HashMap<String, String>();
    
    private static final String EMPTY_STRING = "";

    BaseParameterised(HeaderParser parser) {
        this.parser = parser;        
    }

    /**
     * {@inheritDoc}
     */
    public void resetFromParseString(String string) {
        HeaderDeclaration header = parse(this.parser, string);
        
        this.name = header.getNames().get(0);
        
        this.attributes = header.getAttributes();
        this.directives = header.getDirectives();
    }

    abstract HeaderDeclaration parse(HeaderParser parser, String parseString);

    /**
     * {@inheritDoc}
     */
    public String toParseString() {
        if (this.name == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.name);

        writeMap(this.attributes, sb, "=");
        writeMap(this.directives, sb, ":=");

        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String parseString = toParseString();
        if (parseString == null) {
            return EMPTY_STRING;
        } else {
            return parseString;
        }
    }

    private static void writeMap(Map<String, String> map, StringBuilder sb, String delimiter) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(";").append(entry.getKey()).append(delimiter).append("\"").append(entry.getValue()).append("\"");
        }
    }
    
    public Map<String, String> getDirectives() {
        return this.directives;
    }
    
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + attributes.hashCode();
        result = prime * result + directives.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseParameterised other = (BaseParameterised) obj;
        if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (!directives.equals(other.directives)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }        
}
