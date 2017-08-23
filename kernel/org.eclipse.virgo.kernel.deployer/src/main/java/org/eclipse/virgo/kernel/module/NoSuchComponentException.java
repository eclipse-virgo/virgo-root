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

package org.eclipse.virgo.kernel.module;



/**
 * {@link NoSuchComponentException} is thrown when a component could not be found. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
public final class NoSuchComponentException extends Exception {

    private static final long serialVersionUID = -5201368574907781998L;

    public NoSuchComponentException(String message) {
        super(message);
    }

}
