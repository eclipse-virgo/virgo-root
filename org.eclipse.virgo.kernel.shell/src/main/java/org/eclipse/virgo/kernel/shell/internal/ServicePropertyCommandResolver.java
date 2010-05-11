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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.ServiceReference;


/**
 * A <code>CommandResolver</code> which resolves commands based on service properties.
 *
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class ServicePropertyCommandResolver implements CommandResolver {
    
    private static final String SERVICE_PROPERTY_COMMAND_FUNCTION = "osgi.command.function";

    /** 
     * {@inheritDoc}
     */
    public List<CommandDescriptor> resolveCommands(ServiceReference serviceReference, Object service) {

        String[] commands = (String[])serviceReference.getProperty(SERVICE_PROPERTY_COMMAND_FUNCTION);
        
        if (commands != null) {        
            List<Method> methods = new ArrayList<Method>();
            Class<?> clazz = service.getClass();
    
            for (String command : commands) {
                if (command.endsWith("*")) {
                    methods.addAll(findMethods(command.substring(0, command.length() - 1), clazz));
                } else {
                    Method method = findMethod(command, clazz);
                    if (method != null) {
                        methods.add(method);
                    }
                }
            }
            return createCommandDescriptors(methods, service);
        }
        
        return Collections.<CommandDescriptor>emptyList();        
    }
    
    private static Method findMethod(String methodName, Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        
        return null;
    }
    
    private static List<Method> findMethods(String methodNamePrefix, Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        
        List<Method> matchingMethods = new ArrayList<Method>();
        
        for (Method method : methods) {
            if (method.getName().startsWith(methodNamePrefix)) {
                matchingMethods.add(method);
            }
        }
        
        return matchingMethods;
    }
    
    private static List<CommandDescriptor> createCommandDescriptors(List<Method> methods, Object target) {
        List<CommandDescriptor> commandDescriptors = new ArrayList<CommandDescriptor>();
        
        for (Method method : methods) {
            commandDescriptors.add(new CommandDescriptor(method.getName(), null, method, target));
        }
        
        return commandDescriptors;
    }
}
