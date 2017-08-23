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

import java.lang.reflect.Method;

/**
 * A <code>CommandDescriptor</code> describes a command that is known to the Shell.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *  Thread-safe.
 *
 */
public final class CommandDescriptor {
    
    private final String commandName;
    
    private final String subCommandName;
    
    private final Method method;
    
    private final Object target;
    
    CommandDescriptor(String commandName, String subCommandName, Method method, Object target) {
        this.commandName = commandName;
        this.subCommandName = subCommandName;
        this.method = method;
        this.target = target;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(commandName).append(" ").append(subCommandName).append(" ").append(method);
        return builder.toString();
    }
    
    public String getCommandName() {
        return commandName;
    }

    
    public String getSubCommandName() {
        return subCommandName;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public Object getTarget() {
        return target;
    }
}
