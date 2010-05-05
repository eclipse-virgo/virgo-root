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

package org.eclipse.virgo.repository;

import java.util.Map;
import java.util.Set;

/**
 * <code>Attribute</code>s are applied to an {@link ArtifactDescriptor} to provide additional information about the
 * artifact which it describes.
 * <p />
 * Each <code>Attribute</code> consists of a key, a value and a collection of properties
 * which is a map of keys to <code>Set</code>s of values.
 * Each key and each value is a <code>String</code>. 
 * <p/>Thus an <code>Attribute</code> can be modelled as Key x Value x (Key->setOf(Value)) <br/> 
 * where Key and Value are <code>String</code>s
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread-safe.
 * 
 */
public interface Attribute {

    /**
     * Returns the attribute's key
     * 
     * @return the key
     */
    String getKey();

    /**
     * Returns the attribute's value
     * 
     * @return the value
     */
    String getValue();

    /**
     * Return the attribute's properties. In the event of the attribute having no properties, an empty map is returned.
     * 
     * @return the properties
     */
    public abstract Map<String, Set<String>> getProperties();

}
