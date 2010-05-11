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

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;

/**
 * <p>
 * QuasiLiveBundle represents a bundle that is installed in a live running OSGi 
 * instance, as opposed to one that was captured in a state dump.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of QuasiLiveBundle should be thread-safe
 *
 */
public interface QuasiLiveBundle extends QuasiBundle {

    /**
     * A list of {@link QuasiLiveService}s that are published by this {@link QuasiLiveBundle}
     * 
     * @return list of services
     */
    List<QuasiLiveService> getExportedServices();
    
    /**
     * A list of {@link QuasiLiveService}s that are consumed by this {@link QuasiLiveBundle}
     * 
     * @return list of services
     */
    List<QuasiLiveService> getImportedServices();
    
    /**
     * A string that matches the state the bundle is currently in within OSGi
     * 
     * @return the OSGi state
     */
    String getState();
    
}
