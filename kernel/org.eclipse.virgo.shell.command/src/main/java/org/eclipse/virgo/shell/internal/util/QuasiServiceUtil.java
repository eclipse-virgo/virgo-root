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
package org.eclipse.virgo.shell.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Util methods to help with Service related commands and formatting
 *
 */
public class QuasiServiceUtil {

	private QuasiFrameworkFactory quasiFrameworkFactory;
	private BundleContext bundleContext;

	public QuasiServiceUtil(BundleContext bundleContext, QuasiFrameworkFactory quasiFrameworkFactory) {
		this.bundleContext = bundleContext.getBundle(0l).getBundleContext();
		this.quasiFrameworkFactory = quasiFrameworkFactory;
	}
	
    public ServiceHolder getService(long serviceId) {
    	SortedMap<Long, ServiceHolder> services = getServicesSortedMap();
        return services.get(serviceId);
    }
    
    public List<ServiceHolder> getAllServices() {
        List<ServiceHolder> quasiLiveServices = new ArrayList<ServiceHolder>();
        SortedMap<Long, ServiceHolder> services = getServicesSortedMap();
        for (Entry<Long, ServiceHolder> serviceEntry : services.entrySet()) {
            quasiLiveServices.add(serviceEntry.getValue());
        }
        return quasiLiveServices;
    }

    private SortedMap<Long, ServiceHolder> getServicesSortedMap() {
        SortedMap<Long, ServiceHolder> services = new TreeMap<Long, ServiceHolder>();
        ServiceReference<?>[] allServiceReferences = null;
        try {
            allServiceReferences = this.bundleContext.getAllServiceReferences(null, null);
        } catch (InvalidSyntaxException e) {
            // Will not happen
            return services;
        }
        if(allServiceReferences == null){
            return services;
        }
        for (ServiceReference<?> serviceReference : allServiceReferences) {
        	ServiceHolder service = new ServiceHolder(this.quasiFrameworkFactory.create(), serviceReference);
            services.put(service.getServiceId(), service);
        }
        return services;
    }
}
