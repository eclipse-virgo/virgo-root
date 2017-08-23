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

/**
 * Signals an error detected by the kernel.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 * 
 */
public class KernelException extends Exception {

    private static final long serialVersionUID = -8441774467715137666L;

    /**
     * Creates a new {@link KernelException}.
     * 
     * @param message the error message.
     */
    public KernelException(String message) {
        super(message);
    }

    /**
     * Creates a new {@link KernelException}.
     * 
     * @param message the error message.
     * @param cause the root cause.
     */
    public KernelException(String message, Throwable cause) {
        super(message, cause);
    }

}
