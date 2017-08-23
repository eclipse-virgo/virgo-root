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

package org.eclipse.virgo.shell.internal.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.CommandDescriptor;
import org.eclipse.virgo.shell.internal.CommandRegistry;
import org.eclipse.virgo.shell.internal.help.HelpAccessor;


/**
 * A Shell command that provides help information for all of the commands known to a {@link CommandRegistry}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
@Command("help")
final class HelpCommand {

    private final CommandRegistry commandRegistry;

    private final HelpAccessor helpAccessor;

    HelpCommand(CommandRegistry commandRegistry, HelpAccessor helpAccessor) {
        this.commandRegistry = commandRegistry;
        this.helpAccessor = helpAccessor;
    }

    @Command("")
    public List<String> summaryHelp() {
        List<String> lines = new ArrayList<String>();

        SortedMap<String, CommandDescriptor> topLevelCommands = getTopLevelCommands();
        Set<String> sortedCommandNames = topLevelCommands.keySet();

        int width = maxWidthOfCommandNames(sortedCommandNames);
        String lineFormatNull = String.format("    %%-%ds", width);
        String lineFormat = String.format("    %%-%ds - %%s", width);

        lines.add("");

        for (String commandName : sortedCommandNames) {
            String commandHelp = this.helpAccessor.getSummaryHelp(topLevelCommands.get(commandName).getTarget().getClass());
            if (commandHelp == null) {
                lines.add(String.format(lineFormatNull, commandName));
            } else {
                lines.add(String.format(lineFormat, commandName, commandHelp));
            }
        }

        lines.add("");

        return lines;
    }

    private int maxWidthOfCommandNames(Set<String> keySet) {
        int result = 0;
        for (String key : keySet) {
            if (result < key.length())
                result = key.length();
        }
        return (result < 8 ? 8 : result);
    }

    @Command("")
    public List<String> detailedHelp(String command) {

        Map<String, CommandDescriptor> commandMap = getTopLevelCommands();
        CommandDescriptor descriptor = commandMap.get(command);

        if (descriptor == null) {
            return Arrays.asList(String.format("No help is available as command '%s' is unknown", command));
        }

        List<String> detailedHelp = this.helpAccessor.getDetailedHelp(descriptor.getTarget().getClass());

        if (detailedHelp == null || detailedHelp.size() == 0) {
            return Arrays.asList(String.format("No help is available for command '%s'", command));
        }

        return detailedHelp;
    }

    private SortedMap<String, CommandDescriptor> getTopLevelCommands() {
        SortedMap<String, CommandDescriptor> commandMap = new TreeMap<String, CommandDescriptor>();

        List<CommandDescriptor> commands = this.commandRegistry.getCommandDescriptors();

        for (CommandDescriptor command : commands) {
            commandMap.put(command.getCommandName(), command);
        }

        return commandMap;
    }
}
