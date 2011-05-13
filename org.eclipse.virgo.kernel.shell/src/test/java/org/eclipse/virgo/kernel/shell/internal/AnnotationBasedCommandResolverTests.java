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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.virgo.kernel.shell.Command;
import org.eclipse.virgo.kernel.shell.internal.AnnotationBasedCommandResolver;
import org.eclipse.virgo.kernel.shell.internal.CommandDescriptor;
import org.junit.Test;


/**
 */
public class AnnotationBasedCommandResolverTests {
    
    private final AnnotationBasedCommandResolver resolver = new AnnotationBasedCommandResolver();
    
    @Test
    public void basicAnnotations() throws SecurityException, NoSuchMethodException {
        List<CommandDescriptor> commands = resolver.resolveCommands(null, new TestCommands());
        assertEquals(2, commands.size());
        
        assertCommandEquals("test", "one", TestCommands.class.getMethod("foo"), commands.get(0));
        assertCommandEquals("test", "two", TestCommands.class.getMethod("bar"), commands.get(1));
    }
    
    @Test
    public void inheritedAnnotations() throws SecurityException, NoSuchMethodException {
        List<CommandDescriptor> commands = resolver.resolveCommands(null, new SubTestCommands());
        assertEquals(4, commands.size());
        
        assertCommandEquals("test-sub", "three", SubTestCommands.class.getMethod("alpha"), commands.get(0));
        assertCommandEquals("test-sub", "four", SubTestCommands.class.getMethod("bravo"), commands.get(1));
        assertCommandEquals("test-sub", "one", SubTestCommands.class.getMethod("foo"), commands.get(2));
        assertCommandEquals("test-sub", "two", SubTestCommands.class.getMethod("bar"), commands.get(3));
    }
    
    private void assertCommandEquals(String expectedCommandName, String expectedSubCommandName, Method expectedMethod, CommandDescriptor commandDescriptor) {
        assertEquals(expectedCommandName, commandDescriptor.getCommandName());
        assertEquals(expectedSubCommandName, commandDescriptor.getSubCommandName());
        assertEquals(expectedMethod, commandDescriptor.getMethod());
    }
    
    @Command("test")
    private static class TestCommands {
        
        @SuppressWarnings("unused")
        @Command("one")
        public void foo() {
            
        }
        
        @SuppressWarnings("unused")
        @Command("two")
        public void bar() {
            
        }
    }
    
    @Command("test-sub")
    private static final class SubTestCommands extends TestCommands {
        
        @SuppressWarnings("unused")
        @Command("three")
        public void alpha() {
            
        }
        
        @SuppressWarnings("unused")
        @Command("four")
        public void bravo() {
            
        }
    }
}
