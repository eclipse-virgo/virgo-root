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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;

/**
 * <p>
 * QuasiServiceUtil provides an extension of the Quasi abstraction that allows information on the services 
 * of a live/running OSGi state to be explored. It can also be sorted for when it is being rendered to a 
 * user interface from a set.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * QuasiServiceUtil is threadsafe
 *
 */
public final class ServiceHolder implements Comparable<ServiceHolder> {

    private final ServiceReference<?> serviceReference;
    
    private final QuasiFramework quasiFramework;

    public ServiceHolder(QuasiFramework quasiFramework, ServiceReference<?> serviceReference) {
        this.serviceReference = serviceReference;
        this.quasiFramework = quasiFramework;
    }

    /**
     * {@inheritDoc}
     */
    public long getServiceId() {
        return (Long) this.serviceReference.getProperty(Constants.SERVICE_ID);
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<QuasiBundle> getConsumers() {
        List<QuasiBundle> quasiUsers = new ArrayList<QuasiBundle>();
        Bundle[] usingBundles = this.serviceReference.getUsingBundles();
        if(usingBundles != null){
            for(Bundle user : usingBundles){
            	quasiUsers.add(this.quasiFramework.getBundle(user.getBundleId()));
            }
        }
        return quasiUsers;
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiBundle getProvider() {
        Bundle providingBundle = this.serviceReference.getBundle();
        return this.quasiFramework.getBundle(providingBundle.getBundleId());
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<String, Object>(); 
        for(String key : this.serviceReference.getPropertyKeys()){
            Object value = this.serviceReference.getProperty(key);
            if (value != null) {
                properties.put(key, value);
            }                
        }
        return properties;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(ServiceHolder other) {
        return (int) (this.getServiceId() - other.getServiceId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.serviceReference == null) ? 0 : this.serviceReference.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceHolder other = (ServiceHolder) obj;
        if (this.serviceReference == null) {
            if (other.serviceReference != null){
                return false;
            }
        } else if (!this.serviceReference.equals(other.serviceReference)){
            return false;
        }
        return true;
    }

}
