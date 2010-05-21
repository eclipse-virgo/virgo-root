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

package org.eclipse.virgo.apps.admin.core;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * ServiceHolder represents a single service within OSGi. It will have a single provider and zero to many consumers.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of ServiceHolder should be thread-safe
 *
 */
public interface ServiceHolder extends Comparable<ServiceHolder> {

    /**
     * Return the Id of this service within OSGi.
     * 
     * @return long service Id
     */
    long getServiceId();
    
    String getFormattedObjectClass();
    
    /**
     * A list of {@link BundleHolder}s of bundles that use the service of this {@link ServiceHolder}.
     * 
     * @return List of <code>QuasiLiveBundle</code>s that are using this service
     */
    List<BundleHolder> getConsumers();
    
    /**
     * The {@link BundleHolder} of a bundle that provides the service of this {@link ServiceHolder}.
     * 
     * @return <code>BundleHolder</code> of bundle that registered this service
     */
    BundleHolder getProvider();
    
    /**
     * A map of the Service's properties.
     * 
     * @return The service's properties
     */
    Map<String, String> getProperties();
    
}
