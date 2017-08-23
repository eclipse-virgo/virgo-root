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

/**
 * Represents a header or header entry that can be converted into a parseable string and that can have its values
 * (re)set by parsing a string.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * May not be thread-safe.
 * 
 */
public interface Parseable {

    /**
     * Converts the header or header entry into a parseable <code>String</code>.
     * 
     * @return The header in the form of a parseable String
     */
    String toParseString();

    /**
     * Resets that header's values by parsing the supplied <code>String</code>.
     * 
     * @param string The String to be parsed
     */
    void resetFromParseString(String string);
}
