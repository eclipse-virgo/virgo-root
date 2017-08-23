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

import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.kernel.deployer.core.internal.event.DeploymentListener;



/**
 * TODO Document DeployerRecoveryLogDeploymentListener
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * TODO Document concurrent semantics of DeployerRecoveryLogDeploymentListener
 *
 */
final class DeployerRecoveryLogDeploymentListener implements DeploymentListener {
    
    private final DeployerRecoveryLog recoveryLog;
            
    DeployerRecoveryLogDeploymentListener(DeployerRecoveryLog recoveryLog) {
        this.recoveryLog = recoveryLog;
    }

    /** 
     * {@inheritDoc}
     */
    public void deployed(URI sourceLocation, DeploymentOptions deploymentOptions) {
        this.recoveryLog.add(sourceLocation, deploymentOptions);
    }

    /** 
     * {@inheritDoc}
     */
    public void refreshed(URI sourceLocation) {
        this.recoveryLog.setRedeployFileLastModified();
    }

    /** 
     * {@inheritDoc}
     */
    public void undeployed(URI sourceLocation) {
        this.recoveryLog.remove(sourceLocation);
    }
}
