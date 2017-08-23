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

package org.eclipse.virgo.repository.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;


/**
 * <p>
 * The provided implementation of <code>Attribute</code>. This is a simple data type
 * holding the name and value of an Attribute along with any properties. Simple getters 
 * and a nice toString are provided. This class is immutable.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is Threadsafe
 * 
 */
public final class StandardAttribute implements Attribute {

    private final String name;

    private final String value;

    private final Map<String, Set<String>> properties;

    public StandardAttribute(String name, String value, Map<String, Set<String>> properties) {
        if (name == null || value == null || properties == null) {
            throw new IllegalArgumentException("Arguments can not be null");
        }
        this.name = name;
        this.value = value;
        this.properties = Collections.unmodifiableMap(properties); //prevent any modification
    }

    public StandardAttribute(String name, String value) {
        this(name, value, new HashMap<String, Set<String>>());
    }

    /**
     * {@inheritDoc}
     */
    public String getKey() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public String getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Set<String>> getProperties() {
        return this.properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("'%s=%s' with '%d' properties", this.name, this.value, this.properties.size());
    }

}
