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

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.shell.internal.parsing.ParsedCommand;
import org.eclipse.virgo.shell.internal.parsing.ParsingUtils;

/**
 * The standard implementation of {@link CommandSession}
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * threadsafe
 * 
 */
final class StandardCommandSession implements CommandSession {

    private final CommandInvoker commandInvoker;

    StandardCommandSession(CommandInvoker commandInvoker) {
        this.commandInvoker = commandInvoker;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> execute(CharSequence commandLine) {
        ParsedCommand command = ParsingUtils.parseCommand(commandLine);

        if (command == null) {
            return null;
        }

        try {
            return this.commandInvoker.invokeCommand(command);
        } catch (CommandNotFoundException cnfe) {
            return Arrays.asList(String.format("No command found for input %s", command));
        } catch (ParametersMismatchException pme) {
            return Arrays.asList(pme.getMessage());
        }
    }
}
