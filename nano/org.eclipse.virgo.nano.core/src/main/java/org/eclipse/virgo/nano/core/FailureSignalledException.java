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
 * A <code>FailureSignalledException</code> is used to indicate that a {@link BlockingSignal}
 * has received a failure signal.
 *
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe
 *
 */
public final class FailureSignalledException extends Exception {

    private static final long serialVersionUID = -8980489442577132319L;

    /**
     * Creates a new <code>FailureSignalledException</code> with the supplied failure
     * <code>cause</code>.
     * 
     * @param cause the cause of the failure
     */
    public FailureSignalledException(Throwable cause) {
        super(cause);
    }      
}
