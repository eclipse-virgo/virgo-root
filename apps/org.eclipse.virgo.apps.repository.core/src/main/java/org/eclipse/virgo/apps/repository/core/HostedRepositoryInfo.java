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

package org.eclipse.virgo.apps.repository.core;

import javax.management.MXBean;

/**
 * A management interface for a hosted repository; 
 * exposes information specific to the hosted repository and references underlying repository.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Implementations must be thread-safe.
 *
 */
@MXBean
public interface HostedRepositoryInfo {

    /**
     * @return the hosted repository's Uri prefix
     */
    String getUriPrefix();
    
    /**
     * @return the name of the local repository being hosted
     */
    String getLocalRepositoryName();

}
