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

package org.eclipse.virgo.shell.internal;


/**
 * An exception thrown when a command to be invoked has incorrect parameters.
 * This might be too many, too few, or the wrong types.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 *
 */
public class ParametersMismatchException extends Exception {

    private static final long serialVersionUID = -1264972215460449046L;

    /**
     * 
     */
    public ParametersMismatchException() {
    }
    
    /**
     * @param message Exception detail -- in this case the nature of the mismatch
     */
    public ParametersMismatchException(String message) {
        super(message);
    }

}
