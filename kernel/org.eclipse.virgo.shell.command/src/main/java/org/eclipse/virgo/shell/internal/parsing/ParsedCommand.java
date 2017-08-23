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

package org.eclipse.virgo.shell.internal.parsing;

import java.util.Arrays;

/**
 * A <code>ParsedCommand</code> represents a command that has been parsed into the top-level command and its arguments.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class ParsedCommand {

    private final String command;

    private final String[] arguments;

    public ParsedCommand(String command, String[] arguments) {
        this.command = command;
        this.arguments = arguments.clone();
    }

    public String getCommand() {
        return command;
    }

    public String[] getArguments() {
        return arguments.clone();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(command);

        for (String arg : this.arguments) {
            builder.append(" ").append(arg);
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.arguments);
        result = prime * result + ((this.command == null) ? 0 : this.command.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParsedCommand other = (ParsedCommand) obj;
        if (!Arrays.equals(this.arguments, other.arguments))
            return false;
        if (!this.command.equals(other.command))
            return false;
        return true;
    }

}
