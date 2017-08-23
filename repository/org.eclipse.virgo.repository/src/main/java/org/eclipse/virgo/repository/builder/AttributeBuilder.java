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

package org.eclipse.virgo.repository.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.internal.StandardAttribute;


/**
 * A simple builder API for creating instances of {@link Attribute}.
 * <p />
 * Created <code>Attribute</code>s are created by calling {@link #build()}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not threadsafe. Intended for single-threaded usage.
 * 
 */
public class AttributeBuilder {

    private volatile String name;

    private volatile String value;

    private final Map<String, Set<String>> properties = new HashMap<String, Set<String>>();

    /**
     * Sets the name for the {@link Attribute} being created
     * 
     * @param name the name for the {@link Attribute} being created
     * @return <code>this</code> {@link AttributeBuilder}
     */
    public AttributeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the value for the {@link Attribute} being created
     * 
     * @param value the value for the {@link Attribute} being created
     * @return <code>this</code> {@link AttributeBuilder}
     */
    public AttributeBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Adds a property for the {@link Attribute} being created. Adding different values for a given key is additive and
     * does not overwrite previously added values.
     * 
     * @param key the key for the property being added to the {@link Attribute} being created
     * @param values The values for the property being added to the @{link Attribute} being created
     * @return <code>this</code> {@link AttributeBuilder}
     */
    public AttributeBuilder putProperties(String key, String... values) {
        if (!this.properties.containsKey(key)) {
            this.properties.put(key, new HashSet<String>());
        }

        Set<String> propertiesSet = this.properties.get(key);
        for (String value : values) {
            propertiesSet.add(value);
        }

        return this;
    }

    /**
     * Adds a property for the {@link Attribute} being created. Adding different values for a given key is additive and
     * does not overwrite previously added values.
     * 
     * @param key the key for the property being added to the {@link Attribute} being created
     * @param values The values for the property being added to the @{link Attribute} being created
     * @return <code>this</code> {@link AttributeBuilder}
     */
    public AttributeBuilder putProperties(String key, List<String> values) {
        if (!this.properties.containsKey(key)) {
            this.properties.put(key, new HashSet<String>());
        }

        Set<String> propertiesSet = this.properties.get(key);
        for (String value : values) {
            propertiesSet.add(value);
        }

        return this;
    }

    /**
     * Build an {@link Attribute} from the values set on this builder. Creation of this {@link Attribute} is not
     * guaranteed to complete successfully if some of the values have not been set or are set to illegal values.
     * 
     * @return a new {@link Attribute} created from the values set on this builder
     */
    public Attribute build() {
        return new StandardAttribute(this.name, this.value, properties);
    }
}
