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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.shell.Command;
import org.osgi.framework.ServiceReference;


/**
 * <p>
 * A <code>CommandResolver</code> that examines the OSGi service for {@link Command} annotations to resolve the
 * service's commands.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class AnnotationBasedCommandResolver implements CommandResolver {

    public List<CommandDescriptor> resolveCommands(ServiceReference<?> serviceReference, Object object) {
        List<CommandDescriptor> commandDescriptors = new ArrayList<CommandDescriptor>();

        Class<? extends Object> clazz = object.getClass();
        String commandName = getCommandName(clazz);

        if (commandName != null) {
            while (clazz != null) {
                commandDescriptors.addAll(resolveCommands(clazz, object, commandName));
                clazz = clazz.getSuperclass();
            }
        }

        return commandDescriptors;
    }
    
    private List<CommandDescriptor> resolveCommands(Class<?> clazz, Object object, String commandName) {
        List<CommandDescriptor> commandDescriptors = new ArrayList<CommandDescriptor>();
        
        for (Method method : clazz.getDeclaredMethods()) {
            String subCommandName = getCommandName(method);

            if (subCommandName != null) {
                commandDescriptors.add(new CommandDescriptor(commandName, subCommandName, method, object));
            }
        }
        
        return commandDescriptors;
    }

    private String getCommandName(AnnotatedElement annotatedElement) {
        Command command = annotatedElement.getAnnotation(Command.class);

        if (command == null) {
            return null;
        } else {
            return command.value();
        }
    }
}
