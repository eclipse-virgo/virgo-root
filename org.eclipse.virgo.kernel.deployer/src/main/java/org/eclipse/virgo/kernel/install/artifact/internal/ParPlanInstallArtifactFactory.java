/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphFactory;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.kernel.install.artifact.internal.bundle.BundleInstallArtifactGraphFactory;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.nano.shim.scope.ScopeFactory;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.osgi.framework.BundleContext;

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

    private final BundleInstallArtifactGraphFactory bundleInstallArtifactGraphFactory;

    private final ScopeServiceRepository scopeServiceRepository;

    private final ScopeFactory scopeFactory;

    private final InstallArtifactRefreshHandler refreshHandler;

    private final InstallArtifactGraphFactory configInstallArtifactGraphFactory;

    private final ArtifactStorageFactory artifactStorageFactory;
    
    private final ArtifactIdentityDeterminer artifactIdentityDeterminer;

    private final InstallArtifactGraphFactory planInstallArtifactGraphFactory;

    ParPlanInstallArtifactFactory(EventLogger eventLogger, BundleContext bundleContext,
        BundleInstallArtifactGraphFactory bundleInstallArtifactTreeFactory, ScopeServiceRepository scopeServiceRepository, ScopeFactory scopeFactory,
        InstallArtifactRefreshHandler refreshHandler, ConfigInstallArtifactGraphFactory configInstallArtifactGraphFactory,
        ArtifactStorageFactory artifactStorageFactory, ArtifactIdentityDeterminer artifactIdentityDeterminer, PlanInstallArtifactGraphFactory planInstallArtifactGraphFactory) {
        this.eventLogger = eventLogger;
        this.bundleContext = bundleContext;
        this.bundleInstallArtifactGraphFactory = bundleInstallArtifactTreeFactory;
        this.scopeServiceRepository = scopeServiceRepository;
        this.scopeFactory = scopeFactory;
        this.refreshHandler = refreshHandler;
        this.configInstallArtifactGraphFactory = configInstallArtifactGraphFactory;
        this.artifactStorageFactory = artifactStorageFactory;
        this.artifactIdentityDeterminer = artifactIdentityDeterminer;
        this.planInstallArtifactGraphFactory = planInstallArtifactGraphFactory;
    }

    ParPlanInstallArtifact createParPlanInstallArtifact(@NonNull ArtifactIdentity artifactIdentity, @NonNull ArtifactStorage artifactStorage, String repositoryName) throws DeploymentException {
        ArtifactStateMonitor artifactStateMonitor = new StandardArtifactStateMonitor(this.bundleContext);
        return new ParPlanInstallArtifact(artifactIdentity, artifactStorage, artifactStateMonitor, scopeServiceRepository, scopeFactory, eventLogger,
            bundleInstallArtifactGraphFactory, refreshHandler, repositoryName, this.configInstallArtifactGraphFactory,
            this.artifactStorageFactory, this.artifactIdentityDeterminer, this.planInstallArtifactGraphFactory);
    }
}
