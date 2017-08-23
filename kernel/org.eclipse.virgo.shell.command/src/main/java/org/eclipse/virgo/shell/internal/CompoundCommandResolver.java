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

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.ServiceReference;

/**
 * A <code>CommandResolver</code> that combines results from one or more delegates. 
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class CompoundCommandResolver implements CommandResolver {

    private final CommandResolver[] commandResolvers;

    CompoundCommandResolver(CommandResolver... commandResolvers) {
        this.commandResolvers = commandResolvers;
    }

    /**
     * {@inheritDoc}
     */
    public List<CommandDescriptor> resolveCommands(ServiceReference<?> serviceReference, Object service) {
        List<CommandDescriptor> commandDescriptors = new ArrayList<CommandDescriptor>();

        for (CommandResolver commandResolver : commandResolvers) {
            commandDescriptors.addAll(commandResolver.resolveCommands(serviceReference, service));
        }

        return commandDescriptors;
    }
}
