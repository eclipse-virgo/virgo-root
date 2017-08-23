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
 * Thrown to signal that a command that matches the supplied input could not be found.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 * @see CommandInvoker#invokeCommand(org.eclipse.virgo.shell.internal.parsing.ParsedCommand)
 */
public class CommandNotFoundException extends Exception {
    
    private static final long serialVersionUID = -5788920220311337018L;

    CommandNotFoundException() {
        super();
    }
}
