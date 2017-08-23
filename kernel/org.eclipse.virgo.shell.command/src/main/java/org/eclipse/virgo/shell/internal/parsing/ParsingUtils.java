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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.virgo.util.common.StringUtils;


/**
 * Utility methods for parsing command input.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class ParsingUtils {
    
    private static final String DELIMITER = " ";
    
    private static final String EMPTY_STRING = "";
    
    /**
     * Parses the supplied command.
     * 
     * @param command the command to parse
     * 
     * @return The parsed command.
     */
    public static ParsedCommand parseCommand(CharSequence command) {
        
        String commandString = command.toString();
        String[] tokens = StringUtils.tokenizeToStringArray(commandString, DELIMITER);
        if (tokens.length == 0) {
            return null;
        }

        String commandName = tokens[0];
        String[] arguments = getArguments(commandString.substring(commandName.length()));        
        
        return new ParsedCommand(commandName, arguments);
    }
    
    private static String[] getArguments(String buffer) {
        
        List<String> arguments = new ArrayList<String>();
        
        StringTokenizer tokenizer = new StringTokenizer(buffer, DELIMITER, true);        
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            
            if (DELIMITER.equals(token)) {
                if (!tokenizer.hasMoreElements()) {
                    arguments.add(EMPTY_STRING);
                }
            } else {
                arguments.add(token);
            }
        }
        
        return arguments.toArray(new String[arguments.size()]);
    }
}
