
package org.eclipse.virgo.nano.deployer.internal;

/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   SAP AG - re-factoring
 *******************************************************************************/

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
public final class StandardDeployerConfiguration implements DeployerConfiguration {

    private final int deploymentTimeoutSeconds;

    private final PathReference deploymentPickupDirectory;

    private final int scanIntervalMillis;

    /**
     * Construct a deployment configuration using the given <code>deploymentTimeout</code>, and
     * <code>pickupDirectory</code>.
     * 
     * @param deploymentTimeout The timeout period, in seconds
     * @param pickupDirectory The deployer's pickup directory
     * @param scanIntervalMillis The deployer's scan interval in milliseconds
     */
    public StandardDeployerConfiguration(int deploymentTimeout, File pickupDirectory, int scanIntervalMillis) {
        this.deploymentTimeoutSeconds = deploymentTimeout;
        this.deploymentPickupDirectory = new PathReference(pickupDirectory);
        this.scanIntervalMillis = scanIntervalMillis;
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
    public int getScanIntervalMillis() {
        return this.scanIntervalMillis;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("Timeout: '%s', Pickup directory: '%s', Scan interval in millis: '%s'", this.deploymentTimeoutSeconds,
            this.deploymentPickupDirectory, this.scanIntervalMillis);
    }

}
