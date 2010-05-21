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

package org.eclipse.virgo.apps.admin.core.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Constants;
import org.springframework.util.StringUtils;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.ServiceHolder;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;

/**
 * <p>
 * StandardServiceHolder is the default implementation of ServiceHolder
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardServiceHolder is thread-safe
 *
 */
final class StandardServiceHolder implements ServiceHolder {

    private static final String MULTIPLE_OBJECT_CLASSES_SUFFIX = ", ...";
    
    private final QuasiLiveService quasiLiveService;
    
    private final ModuleContextAccessor moduleContextAccessor;

    public StandardServiceHolder(QuasiLiveService quasiLiveService, ModuleContextAccessor moduleContextAccessor) {
        if(quasiLiveService == null || moduleContextAccessor == null) {
            throw new IllegalArgumentException("QuasiLiveService and ModuleContextAccessor must not be null");
        }
        this.quasiLiveService = quasiLiveService;
        this.moduleContextAccessor = moduleContextAccessor;
    }

    /** 
     * {@inheritDoc}
     */
    public long getServiceId() {
        return this.quasiLiveService.getServiceId();
    }

    /**
     * {@inheritDoc}
     */
    public String getFormattedObjectClass() {
        return this.formatObjectClass(this.quasiLiveService.getProperties().get(Constants.OBJECTCLASS));
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<BundleHolder> getConsumers() {
        List<QuasiLiveBundle> consumers = this.quasiLiveService.getConsumers();
        List<BundleHolder> bundleHolders = new ArrayList<BundleHolder>();
        for(QuasiLiveBundle quasiLiveBundle : consumers) {
            bundleHolders.add(new StandardBundleHolder(quasiLiveBundle, this.moduleContextAccessor));
        }
        return bundleHolders;
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getProperties() {
        return ObjectFormatter.formatMapValues(this.quasiLiveService.getProperties());
    }

    /** 
     * {@inheritDoc}
     */
    public BundleHolder getProvider() {
        return new StandardBundleHolder(this.quasiLiveService.getProvider(), this.moduleContextAccessor);
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(ServiceHolder o) {
        return Long.valueOf(this.getServiceId()).compareTo(Long.valueOf(o.getServiceId()));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + quasiLiveService.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StandardServiceHolder other = (StandardServiceHolder) obj;
        if (!quasiLiveService.equals(other.quasiLiveService))
            return false;
        return true;
    }

    private String formatObjectClass(Object objectClass) {
        StringBuilder sb = new StringBuilder();
        if(objectClass == null) {
            objectClass = new String[0];
        }
        String[] objectClasses;
        if(objectClass instanceof String) {
            objectClasses = StringUtils.commaDelimitedListToStringArray((String) objectClass);
        } else if(objectClass instanceof Object[]) {
            objectClasses = (String[]) objectClass;
        } else {
            objectClasses = StringUtils.commaDelimitedListToStringArray(objectClass.toString());
        }

        if (objectClasses.length == 0) {
            sb.append("<none>");
        } else {

            String formattedObjectClass = objectClasses[0];
            sb.append(formattedObjectClass);

            if (objectClasses.length > 1) {
                sb.append(MULTIPLE_OBJECT_CLASSES_SUFFIX);
            }
        }
        
        return sb.toString();
    }

}
