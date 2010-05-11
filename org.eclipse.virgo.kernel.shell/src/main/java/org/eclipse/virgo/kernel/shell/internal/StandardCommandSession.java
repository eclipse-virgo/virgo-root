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

package org.eclipse.virgo.kernel.shell.internal;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.kernel.shell.internal.parsing.ParsedCommand;
import org.eclipse.virgo.kernel.shell.internal.parsing.ParsingUtils;


/**
 * <p>
 * StandardCommandSession is the standard imple of {@link CommandSession}
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * StandardCommandSession is thread safe
 * 
 */
final class StandardCommandSession implements CommandSession {

    private final PrintStream err;

    private boolean closed = false;

    private final CommandInvoker commandInvoker;

    StandardCommandSession(CommandInvoker commandInvoker, PrintStream err) {
        this.commandInvoker = commandInvoker;
        this.err = err;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> execute(CharSequence commandLine) {
        testOpen();

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

    /**
     * {@inheritDoc}
     */
    public void close() {
        testOpen();
        this.closed = true;
    }

    private void testOpen() {
        if (closed) {
            this.err.print("Command session is already closed");
            throw new IllegalArgumentException("Command session is already closed");
        }
    }

}
