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

import static org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer.PAR_TYPE;
import static org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer.PLAN_TYPE;

import java.io.InputStream;
import java.util.Map;

import org.osgi.framework.BundleContext;


import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor;
import org.eclipse.virgo.kernel.artifact.plan.PlanReader;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
import org.eclipse.virgo.kernel.install.artifact.internal.bundle.BundleInstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.kernel.shim.scope.ScopeFactory;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.io.IOUtils;

/**
 * {@link PlanInstallArtifactTreeFactory} is an {@link InstallArtifactTreeFactory} for plan {@link InstallArtifact
 * InstallArtifacts}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class PlanInstallArtifactTreeFactory implements InstallArtifactTreeFactory {

    private final BundleContext bundleContext;

    private final ScopeServiceRepository scopeServiceRepository;

    private final ScopeFactory scopeFactory;

    private final EventLogger eventLogger;

    private final InstallArtifactRefreshHandler refreshHandler;

    private final ParPlanInstallArtifactFactory parFactory;

    public PlanInstallArtifactTreeFactory(@NonNull BundleContext bundleContext, @NonNull ScopeServiceRepository scopeServiceRepository,
        @NonNull ScopeFactory scopeFactory, @NonNull EventLogger eventLogger,
        @NonNull BundleInstallArtifactTreeFactory bundleInstallArtifactTreeFactory, @NonNull InstallArtifactRefreshHandler refreshHandler,
        @NonNull ConfigInstallArtifactTreeFactory configInstallArtifactTreeFactory, @NonNull ArtifactStorageFactory artifactStorageFactory,
        @NonNull ArtifactIdentityDeterminer artifactIdentityDeterminer) {
        this.bundleContext = bundleContext;
        this.scopeServiceRepository = scopeServiceRepository;
        this.scopeFactory = scopeFactory;
        this.eventLogger = eventLogger;
        this.refreshHandler = refreshHandler;

        this.parFactory = new ParPlanInstallArtifactFactory(eventLogger, bundleContext, bundleInstallArtifactTreeFactory, scopeServiceRepository,
            scopeFactory, refreshHandler, configInstallArtifactTreeFactory, artifactStorageFactory, artifactIdentityDeterminer);
    }

    /**
     * {@inheritDoc}
     */
    public Tree<InstallArtifact> constructInstallArtifactTree(ArtifactIdentity identity, ArtifactStorage artifactStorage,
        Map<String, String> deploymentProperties, String repositoryName) throws DeploymentException {
        String type = identity.getType();
        if (PLAN_TYPE.equalsIgnoreCase(type)) {
            return createPlanTree(identity, artifactStorage, getPlanDescriptor(artifactStorage), repositoryName);
        } else if (PAR_TYPE.equalsIgnoreCase(type)) {
            return createParTree(identity, artifactStorage, repositoryName);
        } else {
            return null;
        }
    }

    private Tree<InstallArtifact> createParTree(ArtifactIdentity artifactIdentity, ArtifactStorage artifactStorage, String repositoryName)
        throws DeploymentException {

        ParPlanInstallArtifact parArtifact = this.parFactory.createParPlanInstallArtifact(artifactIdentity, artifactStorage, repositoryName);
        Tree<InstallArtifact> tree = constructInstallTree(parArtifact);
        parArtifact.setTree(tree);
        return tree;
    }

    /**
     * @throws DeploymentException  
     */
    private PlanDescriptor getPlanDescriptor(ArtifactStorage artifactStorage) throws DeploymentException {
        InputStream in = null;
        try {
            in = artifactStorage.getArtifactFS().getEntry("").getInputStream();
            PlanDescriptor planDescriptor = new PlanReader().read(in);
            return planDescriptor;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private Tree<InstallArtifact> createPlanTree(ArtifactIdentity artifactIdentity, ArtifactStorage artifactStorage, PlanDescriptor planDescriptor,
        String repositoryName) throws DeploymentException {

        StandardPlanInstallArtifact planInstallArtifact;

        planInstallArtifact = new StandardPlanInstallArtifact(artifactIdentity, planDescriptor.getAtomic(), planDescriptor.getScoped(),
            artifactStorage, new StandardArtifactStateMonitor(this.bundleContext), this.scopeServiceRepository, this.scopeFactory,
            this.eventLogger, this.refreshHandler, repositoryName, planDescriptor.getArtifactSpecifications());

        Tree<InstallArtifact> tree = constructInstallTree(planInstallArtifact);
        planInstallArtifact.setTree(tree);
        return tree;
    }

    private Tree<InstallArtifact> constructInstallTree(InstallArtifact rootArtifact) {
        return new ThreadSafeArrayListTree<InstallArtifact>(rootArtifact);
    }

}
