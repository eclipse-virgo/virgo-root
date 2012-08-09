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

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.io.File;

import org.eclipse.virgo.nano.deployer.api.core.DeployerConfiguration;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Standard implementation of {@link DeployerConfiguration}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class StandardDeployerConfiguration implements DeployerConfiguration {    

    private final int deploymentTimeoutSeconds;
    private final PathReference deploymentPickupDirectory;
    
    /**
     * Construct a deployment configuration using the given <code>deploymentTimeout</code>,
     * and <code>pickupDirectory</code>.
     * 
     * @param deploymentTimeout The timeout period, in seconds
     * @param pickupDirectory The deployer's pickup directory
     */
    StandardDeployerConfiguration(int deploymentTimeout, File pickupDirectory) {        
        this.deploymentTimeoutSeconds = deploymentTimeout;
        this.deploymentPickupDirectory = new PathReference(pickupDirectory);
    }

    /**
     * {@inheritDoc}
     */
    public int getDeploymentTimeoutSeconds() {
        return this.deploymentTimeoutSeconds;
    }
    
    /**
     * {@inheritDoc}
     */
    public PathReference getDeploymentPickupDirectory() {
        return this.deploymentPickupDirectory;
    }
    
    /** 
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("Timeout: '%s', Pickup directory: '%s'", this.deploymentTimeoutSeconds, this.deploymentPickupDirectory);
    }
}
