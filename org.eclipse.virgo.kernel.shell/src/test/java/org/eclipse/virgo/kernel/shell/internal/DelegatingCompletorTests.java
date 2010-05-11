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

package org.eclipse.virgo.kernel.shell.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.kernel.shell.CommandCompleter;
import org.eclipse.virgo.kernel.shell.internal.completers.CommandCompleterRegistry;
import org.eclipse.virgo.kernel.shell.internal.completers.DelegatingJLineCompletor;
import org.junit.Test;


/**
 */
public class DelegatingCompletorTests {

    private final CommandCompleterRegistry commandCompleterRegistry = createMock(CommandCompleterRegistry.class);

    private final DelegatingJLineCompletor completor = new DelegatingJLineCompletor(this.commandCompleterRegistry);

    @Test
    public void completionWithNoCommandProvider() {
        expect(commandCompleterRegistry.getCommandCompleter("command")).andReturn(null);
        replay(commandCompleterRegistry);

        List<String> candidates = new ArrayList<String>();

        assertEquals(-1, this.completor.complete("command ", 8, candidates));

        verify(commandCompleterRegistry);
    }

    @Test
    public void completion() {
        expect(commandCompleterRegistry.getCommandCompleter("command")).andReturn(new TestCommandCompletor());
        replay(commandCompleterRegistry);

        List<String> candidates = new ArrayList<String>();

        assertEquals(11, this.completor.complete("command do te", 13, candidates));

        verify(commandCompleterRegistry);
    }

    private static final class TestCommandCompletor implements CommandCompleter {

        /**
         * {@inheritDoc}
         */
        public List<String> getCompletionCandidates(String subCommand, String... arguments) {
            return Arrays.asList("test");
        }
    }
}
