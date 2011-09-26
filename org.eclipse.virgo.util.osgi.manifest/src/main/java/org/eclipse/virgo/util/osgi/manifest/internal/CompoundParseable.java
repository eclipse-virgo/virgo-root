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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.Parameterised;
import org.eclipse.virgo.util.osgi.manifest.Parseable;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParser;


/**
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 */
abstract class CompoundParseable<T extends Parameterised> implements Parseable {

    protected final HeaderParser parser;

    protected List<T> components;
    
    private static final String EMPTY_STRING = "";

    CompoundParseable(HeaderParser parser) {
        this.parser = parser;
        this.components = new ArrayList<T>();
    }

    abstract List<HeaderDeclaration> parse(String parseString);

    abstract T newEntry(String name);

    /**
     * {@inheritDoc}
     */
    public void resetFromParseString(String parseString) {

        this.components.clear();

        if (parseString != null) {
            List<HeaderDeclaration> headers = parse(parseString);

            for (HeaderDeclaration header : headers) {
                for (String name : header.getNames()) {
                    T component = newEntry(name);
                    component.getAttributes().putAll(header.getAttributes());
                    component.getDirectives().putAll(header.getDirectives());
                    this.components.add(component);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toParseString() {
        if (this.components.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();

        Iterator<T> components = this.components.iterator();
        while (components.hasNext()) {
            builder.append(components.next().toParseString());
            if (components.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
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

    protected T add(String name) {
        T newComponent = newEntry(name);
        this.components.add(newComponent);
        return newComponent;
    }
}
