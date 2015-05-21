/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.nano.core.internal;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * Utility class that tracks a set of {@link ServiceReference}s for a bundle and allows them to be safely
 * ungotten.
 * <p/>
 * <code>ServiceReferences</code> are tracked in a thread-safe manner, and are ungotten without
 * holding any locks.
 * 
 * @see ServiceReference
 */
class ServiceReferenceTracker {
    
    private final BundleContext context;
    
    private final Object monitor = new Object();

    private Set<ServiceReference<?>> references; // protected by monitor.

    
    ServiceReferenceTracker(BundleContext context) {
        this.context = context;
    }
    
    /**
     * Tracks the supplied {@link ServiceReference}. This <code>ServiceReference</code> will be
     * {@link BundleContext#ungetService ungotten} during {@link #ungetAll()}.
     * 
     * @param reference the <code>ServiceReference</code> to track.
     * @return the reference itself
     */
    public ServiceReference<?> track(ServiceReference<?> reference) {
        synchronized (this.monitor) {
            if (this.references == null) {
                this.references = new HashSet<ServiceReference<?>>();
            }
            this.references.add(reference);
        }
        return reference;
    }

    /**
     * Safely unregisters all the tracked <code>ServiceRegistrations</code>.
     */
    public void ungetAll() {
        Set<ServiceReference<?>> toUnget = null;
        synchronized (this.monitor) {
            toUnget = this.references;
            this.references = null;
        }
        if (toUnget != null) {
            for (ServiceReference<?> serviceReference : toUnget) {
                try {
                    this.context.ungetService(serviceReference);
                } catch (IllegalStateException e) {
                }
            }
        }
    }
}
