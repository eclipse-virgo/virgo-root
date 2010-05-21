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

package org.eclipse.virgo.apps.repository.core.internal;

import org.eclipse.virgo.apps.repository.core.HostedRepositoryInfo;


/**
 * Standard implementation of the {@link HostedRepositoryInfo} MBean 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 *
 */
class StandardHostedRepositoryInfo implements HostedRepositoryInfo {

    private final String uriPrefix;
    private final String localRepositoryName;
    
    StandardHostedRepositoryInfo(String uriPrefix, String localRepositoryName) {
        this.uriPrefix = uriPrefix;
        this.localRepositoryName = localRepositoryName;
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getLocalRepositoryName() {
        return this.localRepositoryName;
    }

    /** 
     * {@inheritDoc}
     */
    public String getUriPrefix() {
        return this.uriPrefix;
    }

}
