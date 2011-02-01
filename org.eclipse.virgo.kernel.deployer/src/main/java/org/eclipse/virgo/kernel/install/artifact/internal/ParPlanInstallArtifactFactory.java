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

import org.osgi.framework.BundleContext;


import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.kernel.install.artifact.internal.bundle.BundleInstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.kernel.shim.scope.ScopeFactory;
import org.eclipse.virgo.medic.eventlog.EventLogger;

/**
 * A factory for creating {@link ParPlanInstallArtifact} instances.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class ParPlanInstallArtifactFactory {

    private final EventLogger eventLogger;

    private final BundleContext bundleContext;

    private final BundleInstallArtifactTreeFactory bundleInstallArtifactTreeFactory;

    private final ScopeServiceRepository scopeServiceRepository;

    private final ScopeFactory scopeFactory;

    private final InstallArtifactRefreshHandler refreshHandler;

    private final InstallArtifactTreeFactory configInstallArtifactTreeFactory;

    private final ArtifactStorageFactory artifactStorageFactory;
    
    private final ArtifactIdentityDeterminer artifactIdentityDeterminer;

    ParPlanInstallArtifactFactory(EventLogger eventLogger, BundleContext bundleContext,
        BundleInstallArtifactTreeFactory bundleInstallArtifactTreeFactory, ScopeServiceRepository scopeServiceRepository, ScopeFactory scopeFactory,
        InstallArtifactRefreshHandler refreshHandler, ConfigInstallArtifactTreeFactory configInstallArtifactTreeFactory,
        ArtifactStorageFactory artifactStorageFactory, ArtifactIdentityDeterminer artifactIdentityDeterminer) {
        this.eventLogger = eventLogger;
        this.bundleContext = bundleContext;
        this.bundleInstallArtifactTreeFactory = bundleInstallArtifactTreeFactory;
        this.scopeServiceRepository = scopeServiceRepository;
        this.scopeFactory = scopeFactory;
        this.refreshHandler = refreshHandler;
        this.configInstallArtifactTreeFactory = configInstallArtifactTreeFactory;
        this.artifactStorageFactory = artifactStorageFactory;
        this.artifactIdentityDeterminer = artifactIdentityDeterminer;
    }

    ParPlanInstallArtifact createParPlanInstallArtifact(@NonNull ArtifactIdentity artifactIdentity, @NonNull ArtifactStorage artifactStorage, String repositoryName) throws DeploymentException {
        ArtifactStateMonitor artifactStateMonitor = new StandardArtifactStateMonitor(this.bundleContext);
        return new ParPlanInstallArtifact(artifactIdentity, artifactStorage, artifactStateMonitor, scopeServiceRepository, scopeFactory, eventLogger,
            bundleInstallArtifactTreeFactory, refreshHandler, repositoryName, this.configInstallArtifactTreeFactory,
            this.artifactStorageFactory, this.artifactIdentityDeterminer);
    }
}
