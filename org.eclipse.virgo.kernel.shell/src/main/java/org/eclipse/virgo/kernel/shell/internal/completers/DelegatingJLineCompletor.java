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

import org.eclipse.virgo.kernel.shell.CommandCompleter;
import org.eclipse.virgo.kernel.shell.internal.parsing.ParsedCommand;
import org.eclipse.virgo.kernel.shell.internal.parsing.ParsingUtils;

import jline.Completor;


/**
 * A JLine {@link Completor} implementation that delegates to {@link CommandCompleter}s for command parameter completion.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public final class DelegatingJLineCompletor implements Completor {
    
    private static final String EMPTY_STRING = "";
    
    private final CommandCompleterRegistry completerRegistry;
        
    public DelegatingJLineCompletor(CommandCompleterRegistry completerRegistry) {
        this.completerRegistry = completerRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public int complete(String buffer, int cursor, List candidates) {
        if (buffer == null) {
            buffer = EMPTY_STRING;
        }
        
        String toParse = buffer.substring(0, cursor);
        ParsedCommand parsedCommand = ParsingUtils.parseCommand(toParse);

        if (parsedCommand != null) {
            CommandCompleter commandCompleter = this.completerRegistry.getCommandCompleter(parsedCommand.getCommand());
            
            if (commandCompleter != null) {                
                String[] arguments = parsedCommand.getArguments();
                
                String subCommand = null;
                
                if (arguments.length > 0) {
                    subCommand = arguments[0];
                    
                    String[] completionArguments = new String[arguments.length - 1];
                    System.arraycopy(arguments, 1, completionArguments, 0, completionArguments.length);
                    
                    final List<String> newCandidates = commandCompleter.getCompletionCandidates(subCommand, completionArguments);
                    if (newCandidates.size() > 0) {
                        int basePosition = calculatePosition(parsedCommand.getCommand(), arguments);
                        addCandidatesOrExtension(candidates, newCandidates, cursor-basePosition);
                        return basePosition;
                    }
                }
            }                        
        }

        return candidates.isEmpty() ? -1 : 0;
    }
    
    private static int calculatePosition(String command, String[] arguments) {
        int length = command.length() + 1;
        for (int i = 0; i < arguments.length - 1; i++) {
            length += arguments[i].length() + 1;
        }
        return length;
    }

    private static void addCandidatesOrExtension(List<String> candidates, final List<String> newCandidates, int rootLength) {
        if (newCandidates.size() > 1) {
            String newRoot = commonRoot(newCandidates);
            if (newRoot.length() > rootLength) {
                candidates.add(newRoot);
                return;
            }
        }
            
        for (String newCandidate : newCandidates) {
            candidates.add(newCandidate);
        }
    }

    private static String commonRoot(final List<String> newCandidates) {
        StringBuilder sb = new StringBuilder();
        String first = newCandidates.get(0);
        for (int i = 0; i<first.length(); ++i) {
            if (allHaveCharAtPos(newCandidates, first.charAt(i), i)) {
                sb.append(first.charAt(i));
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private static boolean allHaveCharAtPos(final List<String> newCandidates, char testChar, int pos) {
        for (String str : newCandidates) {
            if (pos >= str.length() || str.charAt(pos) != testChar) {
                return false;
            }
        }
        return true;
    }
       

}
