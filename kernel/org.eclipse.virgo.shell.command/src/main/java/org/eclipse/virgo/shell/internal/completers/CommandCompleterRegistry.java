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

package org.eclipse.virgo.shell.internal.completers;

import org.eclipse.virgo.shell.CommandCompleter;

/**
 * A <code>CommandCompleterRegistry</code> provides access to all of the currently available {@link CommandCompleter CommandCompleters}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations must be thread-safe.
 *
 */
public interface CommandCompleterRegistry {

    /**
     * Returns the {@link CommandCompleter} for the command with the given <code>commandName</code>, or
     * <code>null</code> if no such completer is known to the registry.
     * 
     * @param commandName The name of the command for which a completer is required
     * @return The completer for the command
     */
    CommandCompleter getCommandCompleter(String commandName);

}
