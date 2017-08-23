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

package org.eclipse.virgo.nano.core;

import org.eclipse.virgo.nano.serviceability.FatalServerException;

//TODO: merge this with FatalServerException
/**
 * Signals an internal error in the kernel.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 * 
 */
public class FatalKernelException extends FatalServerException {

    private static final long serialVersionUID = 5023385710047479850L;

    /**
     * Creates a new <code>FatalKernelException</code>.
     * 
     * @param message the error message.
     */
    public FatalKernelException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>FatalKernelException</code>.
     * 
     * @param message the error message.
     * @param cause the root cause.
     */
    public FatalKernelException(String message, Throwable cause) {
        super(message, cause);
    }

}
