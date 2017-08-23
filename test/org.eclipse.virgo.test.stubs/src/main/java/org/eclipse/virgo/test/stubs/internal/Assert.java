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

package org.eclipse.virgo.test.stubs.internal;

/**
 * A set of internal assertions for the OSGi test stubs
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class Assert {

    /**
     * Asserts that value is not <code>null</code>. If value is <code>null</null> throws an exception.
     * 
     * @param value The value to test
     * @param argumentName The name of the argument to be placed in the exception message
     * @throws IllegalArgumentException when the <code>value</code> is null
     */
    public static void assertNotNull(Object value, String argumentName) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", argumentName));
        }
    }
}
