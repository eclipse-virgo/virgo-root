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
import java.util.List;

import org.eclipse.virgo.shell.CommandExecutor;
import org.eclipse.virgo.shell.LinePrinter;

/**
 * Implementation of {@link CommandExecutor} that uses a {@link CommandSession} to execute commands.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 *
 * @author Steve Powell
 */
public class SessionCommandExecutor implements CommandExecutor {

    private final static String EXIT_COMMAND = "exit";
    
    private final CommandSession commandSession;
    
    SessionCommandExecutor(CommandSession commandSession) {
        this.commandSession = commandSession;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean execute(String commandLine, LinePrinter linePrinter) throws IOException {
        if (commandLine!=null) {
            commandLine = commandLine.trim();
            if (commandLine.length()>0) {
                if (EXIT_COMMAND.equalsIgnoreCase(commandLine)) {
                    return false;
                } else {
                    try {
                        List<String> executionResult = this.commandSession.execute(commandLine);
                        if (executionResult == null) {
                            linePrinter.println(String.format("Null result returned for '%s'", commandLine));
                        } else {
                            printList(linePrinter, executionResult);
                        }
                    } catch (Exception e) {
                        linePrinter.println(String.format("%s while executing command '%s': '%s'", e.getClass().getName(), commandLine,
                            e.getMessage()));
                    }
                }
            }
        }
        return true;
    }
    
    private static void printList(LinePrinter linePrinter, List<String> executionResult) throws IOException {
        linePrinter.println();
        for (String line : executionResult) {
            linePrinter.println(line);
        }
    }
}
