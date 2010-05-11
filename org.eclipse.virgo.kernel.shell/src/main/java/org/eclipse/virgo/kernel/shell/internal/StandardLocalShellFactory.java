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

import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.virgo.kernel.shell.internal.completers.CommandCompleterRegistry;


/**
 * <p>
 * StandardLocalShellFactory creates new local shells based on JLine and with the 
 * appropriate command information and the provided in/out streams. These shells 
 * can be wrapped to be made available remotely.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * StandardShellFactory is thread safe
 * 
 */
final class StandardLocalShellFactory implements LocalShellFactory {

    private final CommandProcessor commandProcessor;

    private final CommandRegistry commandRegistry;

    private final CommandCompleterRegistry completerRegistry;
    
    /**
     * Constructor taking the port that remote shells should be created on.
     * 
     * @param commandProcessor manages sessions
     * @param commandRegistry in which commands are to be found
     * @param completerRegistry of command completers
     */
    public StandardLocalShellFactory(CommandProcessor commandProcessor, CommandRegistry commandRegistry, CommandCompleterRegistry completerRegistry) {
        this.commandProcessor = commandProcessor;
        this.completerRegistry = completerRegistry;
        this.commandRegistry = commandRegistry;
    }

    /**
     * {@inheritDoc}
     */
    public LocalShell newShell(InputStream in, PrintStream out, PrintStream err) {
        return new JLineLocalShell(this.commandRegistry, this.completerRegistry, this.commandProcessor, in, out, err);
    }
}
