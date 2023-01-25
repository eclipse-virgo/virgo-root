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

package org.eclipse.virgo.util.osgi;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.ServiceRegistration;

/**
 * Utility class that tracks a set of {@link ServiceRegistration ServiceRegistrations} and allows for them to be safely
 * unregistered.
 * <p/>
 * <code>ServiceRegistrations</code> are tracked in a threadsafe manner, and importantly are unregistered without
 * holding any locks.
 * 
 * @see ServiceRegistration
 */
public final class ServiceRegistrationTracker {

    private final Object monitor = new Object();

    private Set<ServiceRegistration<?>> registrations;

    /**
     * Tracks the supplied {@link ServiceRegistration}. This <code>ServiceRegistration</code> will be
     * {@link ServiceRegistration#unregister unregistered} during {@link #unregisterAll()} or 
     * {@link #unregister(ServiceRegistration)}.
     * 
     * @param registration the <code>ServiceRegistration</code> to track.
     */
    public void track(ServiceRegistration<?> registration) {
        synchronized (this.monitor) {
            if (this.registrations == null) {
                this.registrations = new HashSet<ServiceRegistration<?>>();
            }
            this.registrations.add(registration);
        }
    }

    /**
     * Safely unregisters a tracked <code>ServiceRegistration</code>.
     */
    public void unregister(ServiceRegistration<?> registration) {
        synchronized (this.monitor) {
            this.registrations.remove(registration);
        }
        registration.unregister();
    }

    /**
     * Safely unregisters all the tracked <code>ServiceRegistrations</code>.
     */
    public void unregisterAll() {
        Set<ServiceRegistration<?>> toUnregister = null;
        synchronized (this.monitor) {
            toUnregister = this.registrations;
            this.registrations = null;
        }
        if (toUnregister != null) {
            for (ServiceRegistration<?> serviceRegistration : toUnregister) {
                serviceRegistration.unregister();
            }
        }
    }
}
