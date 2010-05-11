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

package org.eclipse.virgo.kernel.shell.internal.completers;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.virgo.kernel.shell.internal.CommandDescriptor;
import org.eclipse.virgo.kernel.shell.internal.CommandRegistry;
import org.eclipse.virgo.kernel.shell.internal.parsing.ParsedCommand;
import org.eclipse.virgo.kernel.shell.internal.parsing.ParsingUtils;

import jline.Completor;


/**
 * A JLine {@link Completor} implementation that offers completions of commands and 
 * sub-commands based on the contents of a {@link CommandRegistry}.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public final class CommandRegistryBackedJLineCompletor implements Completor {

    private final CommandRegistry commandRegistry;

    /**
     * @param commandRegistry
     */
    public CommandRegistryBackedJLineCompletor(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public int complete(String buffer, int cursor, List candidates) {

        String toParse = buffer.substring(0, cursor);
        ParsedCommand parsedCommand = ParsingUtils.parseCommand(toParse);

        if (parsedCommand == null) {
            candidates.addAll(getCommandNames());
        } else {
            String commandName = parsedCommand.getCommand();
            
            String[] arguments = parsedCommand.getArguments();
            
            if (arguments.length == 0) {                
                candidates.addAll(getCommandNameCandidates(commandName));
                return candidates.isEmpty() ? -1 : 0;
            }
            
            if (arguments.length == 1) {
                candidates.addAll(getSubCommandNameCandidates(commandName, arguments[0]));
                if (!candidates.isEmpty()) {
                    return commandName.length() + 1;
                }
            }                        
        }

        return candidates.isEmpty() ? -1 : 0;
    }
    
    private SortedSet<String> getSubCommandNameCandidates(String name, String subName) {
        SortedSet<String> candidates = new TreeSet<String>();
        
        List<CommandDescriptor> commandDescriptors = this.commandRegistry.getCommandDescriptors();
        
        for (CommandDescriptor commandDescriptor : commandDescriptors) {
            if (commandDescriptor.getCommandName().equals(name)) {
                String candidateSubName = commandDescriptor.getSubCommandName();
                
                if (candidateSubName != null && !"".equals(candidateSubName) && candidateSubName.startsWith(subName)) {
                    candidates.add(candidateSubName + " ");
                }                
            }
        }
        
        return candidates;
    }

    private SortedSet<String> getCommandNameCandidates(String name) {
        SortedSet<String> candidates = new TreeSet<String>();
        
        List<CommandDescriptor> commandDescriptors = this.commandRegistry.getCommandDescriptors();
        
        for (CommandDescriptor commandDescriptor : commandDescriptors) {
            if (commandDescriptor.getCommandName().startsWith(name)) {
                String candidate = commandDescriptor.getCommandName() + " ";                
                candidates.add(candidate);
            }
        }
        
        return candidates;
    }

    private SortedSet<String> getCommandNames() {
        SortedSet<String> commandNames = new TreeSet<String>();

        List<CommandDescriptor> commandDescriptors = this.commandRegistry.getCommandDescriptors();

        for (CommandDescriptor commandDescriptor : commandDescriptors) {
            commandNames.add(commandDescriptor.getCommandName());
        }

        return commandNames;
    }
}
