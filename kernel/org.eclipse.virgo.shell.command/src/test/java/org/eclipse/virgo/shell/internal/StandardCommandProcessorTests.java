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

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertNotNull;

import org.eclipse.virgo.shell.internal.CommandInvoker;
import org.eclipse.virgo.shell.internal.CommandSession;
import org.eclipse.virgo.shell.internal.StandardCommandProcessor;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class StandardCommandProcessorTests {

    private StandardCommandProcessor standardCommandProcessor;
    
    /**
     * @throws java.lang.Exception possibly
     */
    @Before
    public void setUp() throws Exception {
        CommandInvoker commandInvoker = createNiceMock(CommandInvoker.class);
        this.standardCommandProcessor = new StandardCommandProcessor(commandInvoker);
    }

    /**
     * Test method for {@link StandardCommandProcessor#createSession()}.
     */
    @Test
    public void testCreateSession() {
        CommandSession commandSession = this.standardCommandProcessor.createSession();
        assertNotNull(commandSession);       
    }
}
