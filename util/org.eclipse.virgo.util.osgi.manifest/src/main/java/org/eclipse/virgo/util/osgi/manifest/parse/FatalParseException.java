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

/**
 * Signals a fatal exception in the parser subsystem.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 * 
 */
public class FatalParseException extends RuntimeException {

    private static final long serialVersionUID = -4743527883152311469L;

    /** 
     * Creates a new <code>FatalParseException</code> with the supplied error message.
     * 
     * @param message The exception's message
     */
    public FatalParseException(String message) {
        super(message);
    }

    /** 
     * Creates a new <code>FatalParseException</code> with the supplied error message and cause.
     * 
     * @param message The exception's message
     * @param cause The exception's cause
     */
    public FatalParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
