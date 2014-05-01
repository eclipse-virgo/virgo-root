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

package org.eclipse.virgo.nano.deployer.api.core;

import org.eclipse.virgo.util.io.PathReference;

/**
 * {@link DeployerConfiguration} provides access to the deployer configuration values.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface DeployerConfiguration {

    /**
     * Get the configured deployment timeout.
     * 
     * @return the timeout in seconds.
     */
    int getDeploymentTimeoutSeconds();

    /**
     * Get the configured deployment pickup directory.
     * 
     * @return the pickup directory as a {@link PathReference}.
     */
    PathReference getDeploymentPickupDirectory();

    /**
     * Get the configured scan interval.
     *
     * @return the scan interval in milliseconds.
     */
    int getScanIntervalMillis();
}
