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

import java.util.List;

import org.eclipse.virgo.shell.internal.parsing.ParsedCommand;



/**
 * A <code>CommandInvoker</code> is used to invoke a command once it has been parsed.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations <strong>must</strong> be thread-safe.
 *
 */
public interface CommandInvoker {
    
    /**
     * Invokes the supplied <code>command</code>.
     * @param command the command to invoke
     * @return the result of invoking the command
     * @throws CommandNotFoundException if a matching command could not be found
     * @throws ParametersMismatchException if the command has too few parameters
     */
    List<String> invokeCommand(ParsedCommand command) throws CommandNotFoundException, ParametersMismatchException;
}
