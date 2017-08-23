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

package org.eclipse.virgo.nano.serviceability;

// TODO This exception doesn't cause a crash
/**
 * Fatal server exceptions are thrown when a severe error is detected in the
 * Server which requires the Server to crash after generating diagnostics. This
 * class is thread safe.
 * 
 * Subsystems should throw their own specific subclasses.
 * 
 */
abstract public class FatalServerException extends RuntimeException {

    /**
     * Stable serial UID.
     */
    private static final long serialVersionUID = -4396605664802060768L;

    /**
     * An aspect will do FFDC when this exception, or subclasses of this
     * exception, is thrown.
     * 
     * @param message an English description of the error
     */
    public FatalServerException(String message) {
        super(message);
    }
    
    public FatalServerException(String message, Throwable cause) {
        super(message, cause);
    }

}
