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
 * <p>
 * StandardCommandProcessor 
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardCommandProcessor is Thread safe
 *
 */
final class StandardCommandProcessor implements CommandProcessor {
    
    private final CommandInvoker commandInvoker;

    /**
     * @param commandInvoker
     */
    StandardCommandProcessor(CommandInvoker commandInvoker) {
        this.commandInvoker = commandInvoker;
    }

    /** 
     * {@inheritDoc}
     */
    public CommandSession createSession() {
        return new StandardCommandSession(commandInvoker);
    }      
}
