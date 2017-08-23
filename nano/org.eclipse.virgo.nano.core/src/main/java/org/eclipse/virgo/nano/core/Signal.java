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
 * {@link Signal} is an interface for signalling successful or unsuccessful completion.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface Signal {

    /**
     * Notifies successful completion. If signalFailure has already been called, the behaviour is undefined.
     */
    void signalSuccessfulCompletion();

    /**
     * Notifies unsuccessful completion with the given {@link Throwable}. If signalCompletion has already been called,
     * the behaviour is undefined.
     * 
     * @param cause a <code>Throwable</code> describing the cause of unsuccessful completion
     */
    void signalFailure(Throwable cause);

}
