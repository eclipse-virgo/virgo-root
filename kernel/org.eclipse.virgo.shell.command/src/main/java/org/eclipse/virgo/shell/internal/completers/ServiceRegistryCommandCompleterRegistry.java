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

package org.eclipse.virgo.shell.internal.completers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.shell.CommandCompleter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;



/**
 * A dynamic registry of {@link CommandCompleter CommandCompleters} backed by the OSGi
 * service registry.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class ServiceRegistryCommandCompleterRegistry implements CommandCompleterRegistry {
        
    private final ServiceListener commandCompleterRegistryServiceListener = new ConverterRegistryServiceListener();
    
    private final Map<String, CommandCompleter> completers = new HashMap<String, CommandCompleter>();
    
    private final Object monitor = new Object();
    
    private final BundleContext bundleContext;
    
    ServiceRegistryCommandCompleterRegistry(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /** 
     * {@inheritDoc}
     */
    public CommandCompleter getCommandCompleter(String commandName) {
        synchronized(this.monitor) {
            return this.completers.get(commandName);
        }
    }
    
    void initialize() {        
        try {
            this.bundleContext.addServiceListener(this.commandCompleterRegistryServiceListener, "(objectClass=" + CommandCompleter.class.getName() + ")");
            ServiceReference<?>[] serviceReferences = this.bundleContext.getServiceReferences(CommandCompleter.class.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference<?> serviceReference : serviceReferences) {
                    serviceRegistered(serviceReference);
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unexpected InvalidSyntaxException", e);
        }
    }
    
    private void serviceRegistered(ServiceReference<?> serviceReference) {
        CommandCompleter completer = (CommandCompleter)bundleContext.getService(serviceReference);
        if (completer != null) {
            String[] commandNames = getCommandNames(serviceReference);
            for (String commandName : commandNames) {
                this.completers.put(commandName, completer);
            }
        }
    }
    
    private String[] getCommandNames(ServiceReference<?> serviceReference) {
        Object commandNamesProperty = serviceReference.getProperty(CommandCompleter.SERVICE_PROPERTY_COMPLETER_COMMAND_NAMES);
        String[] commandNames;
        
        if (commandNamesProperty instanceof String[]) {
            commandNames = (String[])commandNamesProperty;
        } else if (commandNamesProperty instanceof String) {
            commandNames = new String[] {(String)commandNamesProperty};
        } else {
            commandNames = new String[0];
        }
        return commandNames;
    }

    private void serviceUnregistering(ServiceReference<?> serviceReference) {
        Object converter = this.bundleContext.getService(serviceReference);
        if (converter != null) {
            String[] commandNames = getCommandNames(serviceReference);
            synchronized (monitor) {
                for (String commandName : commandNames) {
                    this.completers.remove(commandName);
                }                           
            }
        }
    }
    
    private final class ConverterRegistryServiceListener implements ServiceListener {

        /**
         * {@inheritDoc}
         */
        public void serviceChanged(ServiceEvent event) {
            if (ServiceEvent.REGISTERED == event.getType()) {
                serviceRegistered(event.getServiceReference());
            } else if (ServiceEvent.UNREGISTERING == event.getType()) {
                serviceUnregistering(event.getServiceReference());
            }
        }
    }
}
