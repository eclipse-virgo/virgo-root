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

package org.eclipse.virgo.kernel.deployer.hotenabler;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeployerConfiguration;
import org.eclipse.virgo.nano.deployer.hot.HotDeployer;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Handles hot deployment of application artefacts.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class HotDeployerEnabler implements EventHandler {

    private static final String TOPIC_RECOVERY_COMPLETED = "org/eclipse/virgo/kernel/deployer/recovery/COMPLETED";

    private final HotDeployer hotDeployer;
    
    private final DeployerConfiguration deployerConfig;

    /**
     * Creates a new <code>HotDeployer</code>.
     * 
     * @param deployerConfiguration the {@link DeployerConfiguration} parameters.
     * @param deployer the {@link ApplicationDeployer} to deploy to.
     * @param eventLogger where to log events
     */
    public HotDeployerEnabler(@NonNull DeployerConfiguration deployerConfiguration, @NonNull ApplicationDeployer deployer,
         EventLogger eventLogger) {
        this.deployerConfig = deployerConfiguration;
        this.hotDeployer = new HotDeployer(deployerConfiguration, deployer, eventLogger);
    }

    /**
     * Start the <code>FileSystemWatcher</code>.
     */
    private void doStart() {
        this.hotDeployer.doStart();
    }

    /**
     * Stop the <code>FileSystemWatcher</code>,
     */
    public void stop() {
        this.hotDeployer.doStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Hot Deployer [pickupDir = %s]", this.deployerConfig.getDeploymentPickupDirectory().getAbsolutePath());
    }

    /** 
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
        if (TOPIC_RECOVERY_COMPLETED.equals(event.getTopic())) {
            doStart();
        }    
    }
}
