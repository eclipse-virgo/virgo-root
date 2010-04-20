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

package org.eclipse.virgo.util.osgi.manifest.parse.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;


final class StandardHeaderDeclaration implements HeaderDeclaration {

    private final List<String> names;

    private final Map<String, String> attributes;

    private final Map<String, String> directives;

    /**
     * Creates a new <code>ImmutableHeaderDeclaration</code> that contains a copy of the supplied names, attributes, and
     * directives.
     * 
     * @param names the names in the declaration.
     * @param attributes the attributes in the declaration.
     * @param directives the directives in the declaration.
     */
    StandardHeaderDeclaration(List<String> names, Map<String, String> attributes, Map<String, String> directives) {
        if (names == null) {
            throw new IllegalArgumentException("names must not be null");
        }

        this.names = new ArrayList<String>(names);

        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null");
        }

        this.attributes = new HashMap<String, String>(attributes);

        if (directives == null) {
            throw new IllegalArgumentException("directives must not be null");
        }

        this.directives = new HashMap<String, String>(directives);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getDirectives() {
        return this.directives;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getNames() {
        return this.names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " attributes: " + this.attributes + " directives: " + this.directives + " names: " + this.names;
    }
}
