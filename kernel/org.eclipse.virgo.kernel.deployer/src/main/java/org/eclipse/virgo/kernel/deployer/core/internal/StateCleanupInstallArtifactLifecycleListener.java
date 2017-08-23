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

import java.net.URI;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.core.internal.event.DeploymentListener;
import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;


/**
 * Cleans up the state related to an {@link InstallArtifact} when it is uninstalled.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class StateCleanupInstallArtifactLifecycleListener extends InstallArtifactLifecycleListenerSupport implements InstallArtifactLifecycleListener {

    private final DeploymentListener deploymentListener;

    private final RuntimeArtifactModel runtimeArtifactModel;

    StateCleanupInstallArtifactLifecycleListener(DeploymentListener deploymentListener, RuntimeArtifactModel runtimeArtifactModel) {
        this.deploymentListener = deploymentListener;
        this.runtimeArtifactModel = runtimeArtifactModel;
    }

    @Override
    public void onUninstalled(InstallArtifact installArtifact) throws DeploymentException {
        DeploymentIdentity deploymentIdentity = new StandardDeploymentIdentity(installArtifact.getType(), installArtifact.getName(),
            installArtifact.getVersion().toString());

        URI location = this.runtimeArtifactModel.getLocation(deploymentIdentity);

        if (location != null) {
            this.deploymentListener.undeployed(location);
            this.runtimeArtifactModel.delete(deploymentIdentity);
        }
    }
}
