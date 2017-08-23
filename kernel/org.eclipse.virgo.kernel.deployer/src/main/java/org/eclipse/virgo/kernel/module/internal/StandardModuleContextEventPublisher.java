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

package org.eclipse.virgo.kernel.module.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.nano.core.FatalKernelException;
import org.eclipse.virgo.kernel.module.ModuleContextEvent;
import org.eclipse.virgo.kernel.module.ModuleContextEventListener;
import org.eclipse.virgo.kernel.module.ModuleContextEventPublisher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;


/**
 * {@link StandardModuleContextEventPublisher} is the default implementation of {@link ModuleContextEventPublisher}. It
 * maintains a collection of {@link ModuleContextEventListener ModuleContextEventListeners} and publishes events to
 * those listeners. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class StandardModuleContextEventPublisher implements ModuleContextEventPublisher {
    
    private static final String SERVICE_TYPE_PROPERTY_KEY = "objectclass";
    
    private final Object monitor = new Object();
    
    private Set<ModuleContextEventListener> listeners = new HashSet<ModuleContextEventListener>();

    private final BundleContext bundleContext;
    
    @SuppressWarnings("unchecked")
    StandardModuleContextEventPublisher(BundleContext bundleContext) {
        synchronized (this.monitor) {
            this.bundleContext = bundleContext;
            try {
                ServiceReference<ModuleContextEventListener>[] allServiceReferences = (ServiceReference<ModuleContextEventListener>[]) bundleContext.getAllServiceReferences(ModuleContextEventListener.class.getName(), null);
                if (allServiceReferences != null) {
                    for (ServiceReference<ModuleContextEventListener> serviceReference : allServiceReferences) {
                        registerListener(serviceReference);
                    }
                }
                bundleContext.addServiceListener(new ListenerListener(), "(" + SERVICE_TYPE_PROPERTY_KEY + "=" + ModuleContextEventListener.class.getName() + ")");
            } catch (InvalidSyntaxException e) {
                throw new FatalKernelException("Invalid filter", e);
            }
        }
    }
    
    /**
     * Register a listener.
     * 
     * @param serviceReference the listener service
     */
    private void registerListener(ServiceReference<ModuleContextEventListener> serviceReference) {
        synchronized (this.monitor) {
            this.listeners.add((ModuleContextEventListener)this.bundleContext.getService(serviceReference));
        }
    }
    
    /**
     * Deregisters a listener.
     * 
     * @param serviceReference the listener service
     */
    public void deregisterListener(ServiceReference<ModuleContextEventListener> serviceReference) {
        synchronized (this.monitor) {
            this.listeners.remove((ModuleContextEventListener)this.bundleContext.getService(serviceReference));
            this.bundleContext.ungetService(serviceReference);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onEvent(ModuleContextEvent moduleContextEvent) {
        // Publish event across a copy of the set of listeners to avoid making alien calls.
        for (ModuleContextEventListener moduleContextEventListener : getListeners()) {
            moduleContextEventListener.onEvent(moduleContextEvent);
        }
    }
    
    private Set<ModuleContextEventListener> getListeners() {
        Set<ModuleContextEventListener> l = new HashSet<ModuleContextEventListener>();
        synchronized (this.monitor) {
            l.addAll(this.listeners);
        }
        return l;
    }

    /**
     * Listener which drives register and deregister when listeners are registered and deregistered, respectively.
     */
    private class ListenerListener implements ServiceListener {

        @SuppressWarnings("unchecked")
        public void serviceChanged(ServiceEvent event) {
            synchronized (StandardModuleContextEventPublisher.this.monitor) {
                switch (event.getType()) {
                    case ServiceEvent.REGISTERED:
                        registerListener((ServiceReference<ModuleContextEventListener>) event.getServiceReference());
                        break;
                    case ServiceEvent.UNREGISTERING:
                        deregisterListener((ServiceReference<ModuleContextEventListener>) event.getServiceReference());
                        break;
                    default:
                        break;
                }
            }

        }
    }

}
