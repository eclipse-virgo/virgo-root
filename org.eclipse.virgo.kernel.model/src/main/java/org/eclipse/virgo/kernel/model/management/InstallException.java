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

package org.eclipse.virgo.kernel.model.management;

/**
 * An exception thrown during the process of installing an artifact
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public final class InstallException extends Exception {

    private static final long serialVersionUID = -7001124052781481556L;

    public InstallException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstallException(String message) {
        super(message);
    }

}
