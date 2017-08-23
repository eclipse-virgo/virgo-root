/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.api.core;

import org.eclipse.virgo.nano.serviceability.FatalServerException;

/**
 * Signals a fatal exception in the deployer subsystem.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 * 
 */
public final class FatalDeploymentException extends FatalServerException {

    private static final long serialVersionUID = 8857189976248973019L;

    /** 
     * Creates a new <code>FatalProfileException</code> with the supplied error message.
     * 
     * @param message The exception's message
     */
    public FatalDeploymentException(String message) {
        super(message);
    }

    /** 
     * Creates a new <code>FatalProfileException</code> with the supplied error message and cause.
     * 
     * @param message The exception's message
     * @param cause The exception's cause
     */
    public FatalDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }

}
