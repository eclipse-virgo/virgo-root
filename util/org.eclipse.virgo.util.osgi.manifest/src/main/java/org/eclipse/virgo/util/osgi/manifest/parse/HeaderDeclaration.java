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

package org.eclipse.virgo.util.osgi.manifest.parse;

import java.util.List;
import java.util.Map;

/**
 * Describes a header declaration (Import-Package, Export-Package, Require-Bundle, etc.) in an OSGi manifest header.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementations need not be thread-safe.
 * 
 */
public interface HeaderDeclaration {

    /**
     * Returns a <code>List</code> of the names of the entries in the declaration. If the declaration has no names,
     * an empty <code>Map</code> must be returned, rather than <code>null</code>. 
     * 
     * @return the names, never <code>null</code>.
     */
    List<String> getNames();

    /**
     * Returns a <code>Map</code> of the attributes included in this declaration. If the declaration has
     * no attributes, an empty <code>Map</code> must be returned, rather than <code>null</code>.
     * 
     * @return the attributes.
     */
    Map<String, String> getAttributes();

    /**
     * Returns a <code>Map</code> of the directives included in this declaration. If the declaration has
     * no directives, an empty <code>Map</code> must be returned, rather than <code>null</code>.
     * 
     * @return the directives.
     */
    Map<String, String> getDirectives();
}
