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

package org.eclipse.virgo.util.osgi.manifest;

import java.util.Map;

/**
 * Represents a header or header entry that can be parameterised, i.e. it has attributes and directives.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 */
public interface Parameterised extends Parseable {

    /**
     * Returns a <code>Map</code> of the header or header entry's attributes
     * 
     * @return the attributes
     */
    Map<String, String> getAttributes();

    /**
     * Returns a <code>Map</code> of the header or header entry's directives
     * 
     * @return the directives
     */
    Map<String, String> getDirectives();
}
