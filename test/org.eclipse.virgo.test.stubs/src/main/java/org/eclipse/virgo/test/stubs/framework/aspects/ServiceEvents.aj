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

package org.eclipse.virgo.test.stubs.framework.aspects;

import java.util.List;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.framework.StubServiceRegistration;

/**
 * Sends {@link ServiceEvent}s to {@link ServiceListener}s.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final aspect ServiceEvents {

    /**
     * Sends a {@link ServiceEvent#MODIFIED} event to all of the {@link ServiceListener}s registered with a
     * {@link ServiceRegistration}
     * 
     * @param registration The {@link ServiceRegistration} to send the event against
     */
    after(StubServiceRegistration registration) : 
            this(registration) &&
            execution(* org.eclipse.virgo.test.stubs.framework.StubServiceRegistration.setProperties(*)) {
        sendEvent(registration.getBundleContext().getServiceListeners(), new ServiceEvent(ServiceEvent.MODIFIED, registration.getReference()));
    }

    /**
     * Sends a {@link ServiceEvent#REGISTERED} event to all of the {@link ServiceListener}s registered with a
     * {@link ServiceRegistration}
     * 
     * @param registration The {@link ServiceRegistration} to send the event against
     */
    after(StubBundleContext context) returning (ServiceRegistration<?> registration) :
            this(context) &&
            execution(* org.eclipse.virgo.test.stubs.framework.StubBundleContext.registerService(java.lang.String[], java.lang.Object, java.util.Dictionary)) {
        sendEvent(context.getServiceListeners(), new ServiceEvent(ServiceEvent.REGISTERED, registration.getReference()));
    }

    /**
     * Sends a {@link ServiceEvent#UNREGISTERING} event to all of the {@link ServiceListener}s registered with a
     * {@link ServiceRegistration}
     * 
     * @param registration The {@link ServiceRegistration} to send the event against
     */
    before(StubServiceRegistration registration) :
            this(registration) &&
            execution(* org.eclipse.virgo.test.stubs.framework.StubServiceRegistration.unregister()) {
        sendEvent(registration.getBundleContext().getServiceListeners(), new ServiceEvent(ServiceEvent.UNREGISTERING, registration.getReference()));
    }

    private void sendEvent(List<ServiceListener> listeners, ServiceEvent event) {
        for (ServiceListener listener : listeners) {
            try {
                listener.serviceChanged(event);
            } catch (Exception e) {
                // Swallow exceptions to allow all listeners to be called
            }
        }
    }

}
