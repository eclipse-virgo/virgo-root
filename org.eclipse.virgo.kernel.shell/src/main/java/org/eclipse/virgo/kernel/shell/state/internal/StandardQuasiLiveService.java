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

package org.eclipse.virgo.kernel.shell.state.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;

/**
 * <p>
 * StandardQuasiLiveService is the standard implementation of {@link QuasiLiveService}. It provides 
 * an extension of the Quasi abstraction that allows information on the services of a live/running 
 * OSGi state to be explored. It can also be sorted for when it is being rendered to a user interface 
 * from a set.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardQuasiLiveService is threadsafe
 *
 */
final public class StandardQuasiLiveService implements QuasiLiveService, Comparable<QuasiLiveService> {

    private final ServiceReference serviceReference;
    
    private final QuasiFramework quasiFramework;

    public StandardQuasiLiveService(QuasiFramework quasiFramework, ServiceReference serviceReference) {
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
    public List<QuasiLiveBundle> getConsumers() {
        List<QuasiLiveBundle> quasiUsers = new ArrayList<QuasiLiveBundle>();
        Bundle[] usingBundles = this.serviceReference.getUsingBundles();
        if(usingBundles != null){
            for(Bundle user : usingBundles){
                QuasiBundle quasiUser = this.quasiFramework.getBundle(user.getBundleId());
                quasiUsers.add(new StandardQuasiLiveBundle(this.quasiFramework, quasiUser, user));
            }
        }
        return quasiUsers;
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiLiveBundle getProvider() {
        Bundle providingBundle = this.serviceReference.getBundle();
        QuasiBundle quasiProvidingBundle = this.quasiFramework.getBundle(providingBundle.getBundleId());
        return new StandardQuasiLiveBundle(this.quasiFramework, quasiProvidingBundle, providingBundle);
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
    public int compareTo(QuasiLiveService other) {
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
        StandardQuasiLiveService other = (StandardQuasiLiveService) obj;
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
