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

package org.eclipse.virgo.kernel.deployer.core.internal.recovery;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.nano.deployer.api.core.FatalDeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.ApplicationRecoverer;
import org.eclipse.virgo.medic.eventlog.EventLogger;

/**
 * A <code>RecoveryAgent</code> is an {@link EventHandler} that waits for <code>systemartifacts/DEPLOYED</code> and, if
 * recovery is enabled, drives recovery on all redeploy entries in the recovery state.
 * The recovery is run in another thread.
 * When complete (or if not enabled), fires the <code>recovery/COMPLETED</code> event.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 *
 */
final class RecoveryAgent implements EventHandler {
    
    private static final String TOPIC_SYSTEM_ARTIFACTS_DEPLOYED = "org/eclipse/virgo/kernel/userregion/systemartifacts/DEPLOYED";
    
    private static final String TOPIC_RECOVERY_COMPLETED = "org/eclipse/virgo/kernel/deployer/recovery/COMPLETED";
    
    private final ApplicationRecoverer recoverer;
    
    private final EventLogger eventLogger;
    
    private final EventAdmin eventAdmin;
    
    private final Map<URI, DeploymentOptions> recoveryState;
    
    public RecoveryAgent(ApplicationRecoverer recoverer, DeployerRecoveryLog recoveryLog, EventLogger eventLogger, EventAdmin eventAdmin) {
        this.recoverer = recoverer;
        this.recoveryState = recoveryLog.getRecoveryState();
        this.eventLogger = eventLogger;
        this.eventAdmin = eventAdmin;
    }

    void performRecovery() {                
        if (isRecoveryEnabled()) {
            Thread recoveryThread = new Thread(new Runnable() {
                public void run() {
                	try {	                    
	                    for (Entry<URI, DeploymentOptions> redeployEntry : recoveryState.entrySet()) {
	                        URI uri = redeployEntry.getKey();
	                        DeploymentOptions deploymentOptions = redeployEntry.getValue();
	                        try {
	                            recoverer.recoverDeployment(uri, deploymentOptions);
	                        } catch (DeploymentException e) {
	                            eventLogger.log(DeployerLogEvents.RECOVERY_FAILED, e, uri);
	                        } catch (FatalDeploymentException e) {
	                            eventLogger.log(DeployerLogEvents.RECOVERY_FAILED, e, uri);
	                        }
	                    }
                	} finally {
                		recoveryComplete();
                	}
                }
            }, "deployer-recovery");
            recoveryThread.start();
        } else {
            recoveryComplete();
        }
    }

	private void recoveryComplete() {
		this.recoveryState.clear();
		eventAdmin.postEvent(new Event(TOPIC_RECOVERY_COMPLETED, (Map<String, ?>)null));
	}
    
    private boolean isRecoveryEnabled() {
        return !Boolean.valueOf(FrameworkUtil.getBundle(getClass()).getBundleContext().getProperty("org.eclipse.virgo.kernel.deployer.disableRecovery"));
    }

    /** 
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
        if (TOPIC_SYSTEM_ARTIFACTS_DEPLOYED.equals(event.getTopic())) {
            performRecovery();
        }
    }
}
