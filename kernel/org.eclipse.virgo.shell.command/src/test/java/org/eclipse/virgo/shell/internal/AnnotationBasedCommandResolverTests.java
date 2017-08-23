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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.AnnotationBasedCommandResolver;
import org.eclipse.virgo.shell.internal.CommandDescriptor;
import org.junit.Test;

/**
 */
public class AnnotationBasedCommandResolverTests {

    private final AnnotationBasedCommandResolver resolver = new AnnotationBasedCommandResolver();

    @Test
    public void basicAnnotations() throws SecurityException, NoSuchMethodException {
        List<CommandDescriptor> commands = resolver.resolveCommands(null, new TestCommands());
        assertEquals(2, commands.size());

        assertCommandEquals(TestCommands.class.getMethod("foo"), "test", "one", commands);
        assertCommandEquals(TestCommands.class.getMethod("bar"), "test", "two", commands);
    }

    @Test
    public void inheritedAnnotations() throws SecurityException, NoSuchMethodException {
        List<CommandDescriptor> commands = resolver.resolveCommands(null, new SubTestCommands());
        assertEquals(4, commands.size());

        assertCommandEquals(SubTestCommands.class.getMethod("alpha"), "test-sub", "three", commands);
        assertCommandEquals(SubTestCommands.class.getMethod("bravo"), "test-sub", "four", commands);
        assertCommandEquals(SubTestCommands.class.getMethod("foo"), "test-sub", "one", commands);
        assertCommandEquals(SubTestCommands.class.getMethod("bar"), "test-sub", "two", commands);
    }

    private void assertCommandEquals(Method expectedMethod, String expectedCommandName, String expectedSubCommandName,
        List<CommandDescriptor> commands) {
        boolean found = false;
        for (CommandDescriptor commandDescriptor : commands) {
            if (expectedMethod.equals(commandDescriptor.getMethod())) {
                found = true;
                assertEquals(expectedCommandName, commandDescriptor.getCommandName());
                assertEquals(expectedSubCommandName, commandDescriptor.getSubCommandName());
            }
        }
        assertTrue("Method not found '" + expectedMethod + "'", found);
    }

    @Command("test")
    private static class TestCommands {

        @Command("one")
        public void foo() {

        }

        @Command("two")
        public void bar() {

        }
    }

    @Command("test-sub")
    private static final class SubTestCommands extends TestCommands {

        @Command("three")
        public void alpha() {

        }

        @Command("four")
        public void bravo() {

        }
    }
}
