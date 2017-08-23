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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.shell.internal.CommandInvoker;
import org.eclipse.virgo.shell.internal.CommandNotFoundException;
import org.eclipse.virgo.shell.internal.ParametersMismatchException;
import org.eclipse.virgo.shell.internal.StandardCommandSession;
import org.eclipse.virgo.shell.internal.parsing.ParsedCommand;
import org.eclipse.virgo.shell.internal.parsing.ParsingUtils;
import org.junit.Test;


/**
 */
public class StandardCommandSessionTests {

    private final CommandInvoker commandInvoker = createMock(CommandInvoker.class);

    private final StandardCommandSession standardCommandSession = new StandardCommandSession(commandInvoker);

    @Test
    public void testExecute() throws CommandNotFoundException, ParametersMismatchException {
        ParsedCommand parsedCommand = ParsingUtils.parseCommand("bundle examine 5");

        expect(this.commandInvoker.invokeCommand(eq(parsedCommand))).andReturn(Arrays.asList("result"));
        replay(this.commandInvoker);

        List<String> lines = this.standardCommandSession.execute("bundle examine 5");
        assertEquals("result", lines.get(0));
    }
}
