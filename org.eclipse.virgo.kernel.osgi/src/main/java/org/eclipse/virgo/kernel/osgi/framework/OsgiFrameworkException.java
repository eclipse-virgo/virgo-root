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

package org.eclipse.virgo.kernel.osgi.framework;

/**
 * Signals a generic error in the OSGi Framework subsystem.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public class OsgiFrameworkException extends RuntimeException {

    private static final long serialVersionUID = -2615790102302563284L;

    /**
     * Creates a new <code>OsgiFrameworkException</code>.
     */
    public OsgiFrameworkException() {
        super();
    }

    /**
     * Creates a new <code>OsgiFrameworkException</code>.
     * 
     * @param message the error message.
     * @param cause the root cause.
     */
    public OsgiFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new <code>OsgiFrameworkException</code>.
     * 
     * @param message the error message.
     */
    public OsgiFrameworkException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>OsgiFrameworkException</code>.
     * 
     * @param cause the root cause.
     */
    public OsgiFrameworkException(Throwable cause) {
        super(cause);
    }

}
