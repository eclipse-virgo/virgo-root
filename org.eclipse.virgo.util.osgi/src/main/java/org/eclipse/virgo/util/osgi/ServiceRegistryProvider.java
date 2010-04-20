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

import org.eclipse.virgo.util.common.SetProvider;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;




/**
 * <p>
 * This class provides a wrapper around the OSGi <code>ServiceTracker</code> class that 
 * lets you request all instance of a service at any time.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is thread safe.
 *
 * @param <T> parametric type
 */
public class ServiceRegistryProvider<T> implements SetProvider<T> {

    private final ServiceTracker serviceTracker;

    private final Class<T> typeClazz;
    
    /**
     * Requires a bundle context to access the service registry trough. This should be the
     * bundle context belonging to the code using this service, not the bundle that is 
     * publishing the services of interest.
     * 
     * @param bundleContext of code using this service
     * @param clazz Class of service
     */
    public ServiceRegistryProvider(BundleContext bundleContext, Class<T> clazz) {
        this.typeClazz = clazz;
        this.serviceTracker = new ServiceTracker(bundleContext, this.typeClazz.getName(), null);
        this.serviceTracker.open();
    }

    /** 
     * {@inheritDoc}
     */
    public Set<T> getSet() {
        Object[] services = serviceTracker.getServices();
        Set<T> servicesSet = new HashSet<T>();
        if (services != null) {
            for (Object provider : services) {
                servicesSet.add(this.typeClazz.cast(provider));
            }
        }

        return servicesSet;
    }

    /**
     * Close the underlying <code>ServiceTracker</code> object.
     * 
     */
    public void close() {
        if (serviceTracker != null) {
            serviceTracker.close();
        }
    }
    
}
