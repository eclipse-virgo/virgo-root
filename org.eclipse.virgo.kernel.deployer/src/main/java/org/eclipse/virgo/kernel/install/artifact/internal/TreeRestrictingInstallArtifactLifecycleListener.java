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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.util.concurrent.ConcurrentHashMap;


import org.eclipse.virgo.kernel.deployer.core.DeployerLogEvents;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.Tree;

/**
 * {@link TreeRestrictingInstallArtifactLifecycleListener} fails any install which would turn the forest of
 * {@link InstallArtifact} trees into a more general directed acyclic graph.
 * <p/>
 * Before this restriction was put in place, various failures were possible. For example, a shared node could be
 * uninstalled prematurely.
 * <p/>
 * In order to remove this restriction, this class must be deleted, the forest must be changed to a DAG, reference and
 * possibly other lifecycle counts must be implemented for shared nodes in the DAG, and all algorithms which traverse
 * from a node to its parent must be generalised to cope with multiple parents.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class TreeRestrictingInstallArtifactLifecycleListener extends InstallArtifactLifecycleListenerSupport {

    private final ConcurrentHashMap<ArtifactIdentity, InstallArtifact> artifactMap = new ConcurrentHashMap<ArtifactIdentity, InstallArtifact>();

    private final EventLogger eventLogger;

    public TreeRestrictingInstallArtifactLifecycleListener(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInstalling(InstallArtifact installArtifact) throws DeploymentException {
        ArtifactIdentity artifactIdentity = getArtifactIdentity(installArtifact);
        InstallArtifact oldInstallArtifact = this.artifactMap.putIfAbsent(artifactIdentity, installArtifact);

        if (oldInstallArtifact != null) {
            InstallArtifact oldRootInstallArtifact = getRoot(oldInstallArtifact);

            this.eventLogger.log(DeployerLogEvents.INSTALL_ARTIFACT_DAG_NOT_SUPPORTED, artifactIdentity.getType(), artifactIdentity.getName(),
                artifactIdentity.getVersion(), oldRootInstallArtifact.getType(), oldRootInstallArtifact.getName(),
                oldRootInstallArtifact.getVersion());

            throw new DeploymentException("InstallArtifact " + artifactIdentity + " was installed when "
                + getArtifactIdentity(oldRootInstallArtifact) + " was installed", true);
        }
    }

    private ArtifactIdentity getArtifactIdentity(InstallArtifact installArtifact) {
        return new ArtifactIdentity(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(), installArtifact.getScopeName());
    }

    private InstallArtifact getRoot(InstallArtifact installArtifact) {
        InstallArtifact root = installArtifact;
        Tree<InstallArtifact> rootParent = root.getTree();
        while (rootParent != null) {
            root = rootParent.getValue();
            rootParent = rootParent.getParent();
        }
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInstallFailed(InstallArtifact installArtifact) throws DeploymentException {
        remove(installArtifact);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUninstalled(InstallArtifact installArtifact) throws DeploymentException {
        remove(installArtifact);
    }

    private void remove(InstallArtifact installArtifact) {
        ArtifactIdentity artifactIdentity = getArtifactIdentity(installArtifact);
        this.artifactMap.remove(artifactIdentity);
    }

}
