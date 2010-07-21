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

import java.util.Map;

import org.osgi.framework.BundleContext;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;

import org.eclipse.virgo.kernel.core.BundleStarter;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.InstallArtifactRefreshHandler;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.kernel.shim.serviceability.TracingService;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;

/**
 * {@link BundleInstallArtifactTreeFactory} is an {@link InstallArtifactTreeFactory} for {@link BundleInstallArtifact
 * BundleInstallArtifacts}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class BundleInstallArtifactTreeFactory implements InstallArtifactTreeFactory {

    private final BundleInstallArtifactFactory bundleArtifactFactory;

    public BundleInstallArtifactTreeFactory(@NonNull OsgiFramework osgiFramework, @NonNull BundleContext kernelBundleContext,
        @NonNull InstallArtifactRefreshHandler refreshHandler, @NonNull BundleStarter bundleStarter, @NonNull TracingService tracingService,
        @NonNull PackageAdminUtil packageAdminUtil, @NonNull BundleContext regionBundleContext, EventLogger eventLogger, ArtifactIdentityDeterminer identityDeterminer) {

        BundleDriverFactory bundleDriverFactory = new BundleDriverFactory(osgiFramework, regionBundleContext, bundleStarter, tracingService,
            packageAdminUtil);

        this.bundleArtifactFactory = new BundleInstallArtifactFactory(kernelBundleContext, refreshHandler, bundleDriverFactory, eventLogger, identityDeterminer);
    }

    /**
     * {@inheritDoc}
     */
    public Tree<InstallArtifact> constructInstallArtifactTree(ArtifactIdentity identity, ArtifactStorage artifactStorage, Map<String, String> deploymentProperties,
        String repositoryName) throws DeploymentException {
        if (ArtifactIdentityDeterminer.BUNDLE_TYPE.equalsIgnoreCase(identity.getType())) {
            BundleInstallArtifact bundleInstallArtifact = this.bundleArtifactFactory.createBundleInstallArtifact(identity, artifactStorage, repositoryName);

            if (deploymentProperties != null) {
                bundleInstallArtifact.getDeploymentProperties().putAll(deploymentProperties);
            }

            Tree<InstallArtifact> tree = constructInstallTree(bundleInstallArtifact);
            ((StandardBundleInstallArtifact) bundleInstallArtifact).setTree(tree);
            return tree;
        } else {
            return null;
        }
    }

    private Tree<InstallArtifact> constructInstallTree(InstallArtifact rootArtifact) {
        return new ThreadSafeArrayListTree<InstallArtifact>(rootArtifact);
    }
}
