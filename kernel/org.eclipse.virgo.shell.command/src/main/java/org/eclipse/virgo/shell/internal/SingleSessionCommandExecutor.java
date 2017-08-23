/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.shell.internal;

import java.io.IOException;

import org.eclipse.virgo.shell.CommandExecutor;
import org.eclipse.virgo.shell.LinePrinter;

/**
 * Implementation of {@link CommandExecutor} which executes a single command in a new session.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 *
 * @author Steve Powell
 */
public class SingleSessionCommandExecutor implements CommandExecutor {

    private final CommandProcessor commandProcessor;
    
    SingleSessionCommandExecutor(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean execute(String commandLine, LinePrinter linePrinter) throws IOException {
        CommandExecutor sessionCommandExecutor = new SessionCommandExecutor(this.commandProcessor.createSession());
        return sessionCommandExecutor.execute(commandLine, linePrinter);
    }

}
