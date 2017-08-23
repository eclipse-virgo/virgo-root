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

package org.eclipse.virgo.kernel.install.artifact.internal.bundle;

import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStateMonitor;
import org.eclipse.virgo.kernel.install.artifact.internal.StandardArtifactStateMonitor;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.math.ConcurrentHashSet;

/**
 * {@link BundleDriverBundleListener} listens for bundle events and notifies the bundle's {@link StandardArtifactStateMonitor}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class BundleDriverBundleListener implements SynchronousBundleListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StandardBundleInstallArtifact installArtifact;

    private final Bundle bundle;

    private final ArtifactStateMonitor artifactStateMonitor;

    private final Set<Bundle> solicitedStartBundleSet = new ConcurrentHashSet<Bundle>();

    public BundleDriverBundleListener(@NonNull StandardBundleInstallArtifact installArtifact, @NonNull Bundle bundle,
        @NonNull ArtifactStateMonitor artifactStateMonitor) {
        this.installArtifact = installArtifact;
        this.bundle = bundle;
        this.artifactStateMonitor = artifactStateMonitor;
    }

    void addSolicitedStart(Bundle bundle) {
        this.solicitedStartBundleSet.add(bundle);
    }

    void removeSolicitedStart(Bundle bundle) {
        this.solicitedStartBundleSet.remove(bundle);
    }

    /**
     * {@inheritDoc}
     */
    public void bundleChanged(BundleEvent event) {
        if (event.getBundle() == this.bundle) {
            try {
                switch (event.getType()) {
                    case BundleEvent.RESOLVED:
                        artifactStateMonitor.onResolved(this.installArtifact);
                        break;
                    case BundleEvent.LAZY_ACTIVATION:
                        break;
                    case BundleEvent.STARTING:
                        artifactStateMonitor.onStarting(this.installArtifact);
                        break;
                    case BundleEvent.STARTED:
                        if (!this.solicitedStartBundleSet.contains(this.bundle)) {
                            /*
                             * Track an unsolicited start of the install artifact so that its state transitions are performed
                             * correctly. Solicited starts are tracked by the solicitor.
                             */
                            this.installArtifact.trackStart();
                        }
                        break;
                    case BundleEvent.STOPPING:
                        artifactStateMonitor.onStopping(this.installArtifact);
                        break;
                    case BundleEvent.STOPPED:
                        artifactStateMonitor.onStopped(this.installArtifact);
                        break;
                    case BundleEvent.UNRESOLVED:
                        artifactStateMonitor.onUnresolved(this.installArtifact);
                        break;
                    case BundleEvent.UNINSTALLED:
                        artifactStateMonitor.onUninstalled(this.installArtifact);
                        break;
                    default:
                        break;
                }
            } catch (DeploymentException e) {
                logger.error(String.format("listener for bundle %s threw DeploymentException", this.bundle), e);
                throw new RuntimeException("percolated listener exception", e);
            }
        }
    }
}
