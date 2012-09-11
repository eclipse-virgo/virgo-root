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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.virgo.shell.CommandCompleter;
import org.eclipse.virgo.shell.internal.CommandDescriptor;
import org.eclipse.virgo.shell.internal.CommandRegistry;


/**
 * A <code>CommandCompleter<code> for the <code>help</code> command that offers completions based on all the commands in
 * a {@link CommandRegistry}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class HelpCommandCompleter implements CommandCompleter {

    private final CommandRegistry commandRegistry;

    HelpCommandCompleter(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getCompletionCandidates(String subCommand, String... arguments) {
        List<String> candidates = new ArrayList<String>();
        if (arguments.length == 0) {
            // only complete first argument (subCommand)
            List<CommandDescriptor> commandDescriptors = this.commandRegistry.getCommandDescriptors();
            for (CommandDescriptor commandDescriptor : commandDescriptors) {
                if (commandDescriptor.getCommandName().startsWith(subCommand)) {
                    candidates.add(commandDescriptor.getCommandName());
                }
            }
        }
        Collections.sort(candidates);
        return candidates;
    }
}
