/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.osgi.console.supportability;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements basic command completion. It can complete only OSGi commands, not command parameters. It
 * registers a tracker, with which it tracks all CommandProvider services, and when a new one becomes available, it adds
 * its command methods in a local cache, which the completer uses to complete the command name given the first letters
 * of the command.
 */
public class CommandCompleter {

    private Set<String> availableCommands;

    private ServiceTracker<CommandProvider, Object> cpTracker;

    private BundleContext context = null;

    public CommandCompleter(BundleContext context) {
        this.context = context;
        availableCommands = new HashSet<String>();
        availableCommands = Collections.synchronizedSet(availableCommands);
        availableCommands.add("more");
        availableCommands.add("disconnect");
        availableCommands.add("grep");
        if (context != null) {
            cpTracker = new ServiceTracker<CommandProvider, Object>(context, CommandProvider.class.getName(), new CommandProviderCustomizer());
            cpTracker.open();
        }
    }

    public String[] complete(String prefix) {
        ArrayList<String> candidates = new ArrayList<String>();
        for (String command : availableCommands) {
            if (command.startsWith(prefix)) {
                candidates.add(command);
            }
        }

        return candidates.toArray(new String[candidates.size()]);
    }

    class CommandProviderCustomizer implements ServiceTrackerCustomizer<CommandProvider, Object> {

        public Object addingService(ServiceReference<CommandProvider> reference) {
            CommandProvider provider = context.getService(reference);
            Method[] methods = provider.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("_")) {
                    availableCommands.add(method.getName().substring(1));
                }
            }
            return null;
        }

        public void modifiedService(ServiceReference<CommandProvider> reference, Object service) {
            // do nothing
        }

        public void removedService(ServiceReference<CommandProvider> reference, Object service) {
            CommandProvider provider = context.getService(reference);
            Method[] methods = provider.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("_")) {
                    availableCommands.remove(method.getName());
                }
            }
        }
    }
}
