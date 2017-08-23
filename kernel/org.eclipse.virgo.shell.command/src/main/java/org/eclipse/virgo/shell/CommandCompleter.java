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

package org.eclipse.virgo.shell;

import java.util.List;

/**
 * A <code>CommandCompleter</code> is used to help the user complete a command. A <code>CommandCompleter</code> can be
 * made available to the Shell by publishing it in the service registry and setting the required
 * {@link #SERVICE_PROPERTY_COMPLETER_COMMAND_NAMES service property}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public interface CommandCompleter {

    /**
     * A service property used to specify the commands with which the completer should be associated. The property's
     * value must be either a String or a String[].
     */
    public static final String SERVICE_PROPERTY_COMPLETER_COMMAND_NAMES = "org.eclipse.virgo.shell.completer.commmandNames";

    /**
     * Returns all of the completion candidates for the given <code>subCommand</code> and <code>arguments</code>. The
     * argument which the user is attempting to complete is always the last argument. If the user is attempting to
     * complete the first argument, <code>arguments</code> will contain a single entry that is an empty String, i.e.
     * <code>arguments</code> will always have length >= 1, and will never contain <code>null</code> entries.
     * 
     * @param subCommand The subCommand, or <code>null</code> if there is no sub-command.
     * 
     * @param arguments The arguments that the user has entered thus far
     * 
     * @return The completion candidates. If there are no candidates, an empty array is returned, <strong>not</strong>
     *         null.
     */
    List<String> getCompletionCandidates(String subCommand, String... arguments);
}
