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

package org.eclipse.virgo.kernel.shell.state;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * QuasiLiveService represents a service in the live running OSGi state
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of QuasiLiveService should be thread-safe
 *
 */
public interface QuasiLiveService extends Comparable<QuasiLiveService> {

    /**
     * Return the Id of this service within OSGi.
     * 
     * @return long service Id
     */
    long getServiceId();
    
    /**
     * A list of {@link QuasiLiveBundle}s that use this {@link QuasiLiveService}.
     * 
     * @return List of <code>QuasiLiveBundle</code>s that are using this service
     */
    List<QuasiLiveBundle> getConsumers();
    
    /**
     * The {@link QuasiLiveBundle} that provides this {@link QuasiLiveService}.
     * 
     * @return <code>QuasiLiveBundle</code> that registered this service
     */
    QuasiLiveBundle getProvider();
    
    /**
     * A map of the Service's properties.
     * 
     * @return The service's properties
     */
    Map<String, Object> getProperties();
    
}
