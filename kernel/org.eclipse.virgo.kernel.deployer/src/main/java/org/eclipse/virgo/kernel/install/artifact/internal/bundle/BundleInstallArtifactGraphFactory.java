/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution (BundleInstallArtifactTreeFactory)
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal.bundle;

import java.util.Map;

import org.eclipse.virgo.nano.core.BundleStarter;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractArtifactGraphFactory;
import org.eclipse.virgo.kernel.install.artifact.internal.InstallArtifactRefreshHandler;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.GraphNode;
import org.osgi.framework.BundleContext;

/**
 * {@link BundleInstallArtifactGraphFactory} is an {@link InstallArtifactGraphFactory} for {@link BundleInstallArtifact
 * BundleInstallArtifacts}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class BundleInstallArtifactGraphFactory extends AbstractArtifactGraphFactory {

    private final BundleInstallArtifactFactory bundleArtifactFactory;
    public BundleInstallArtifactGraphFactory(@NonNull OsgiFramework osgiFramework, @NonNull BundleContext kernelBundleContext,
        @NonNull InstallArtifactRefreshHandler refreshHandler, @NonNull BundleStarter bundleStarter, @NonNull TracingService tracingService,
        @NonNull PackageAdminUtil packageAdminUtil, @NonNull BundleContext regionBundleContext, EventLogger eventLogger, ArtifactIdentityDeterminer identityDeterminer,
        @NonNull DirectedAcyclicGraph<InstallArtifact> dag) {

    		super(dag);
        BundleDriverFactory bundleDriverFactory = new BundleDriverFactory(osgiFramework, regionBundleContext, bundleStarter, tracingService,
            packageAdminUtil);

        this.bundleArtifactFactory = new BundleInstallArtifactFactory(kernelBundleContext, refreshHandler, bundleDriverFactory, eventLogger, identityDeterminer);
    }

    /**
     * {@inheritDoc}
     */
    public GraphNode<InstallArtifact> constructInstallArtifactGraph(ArtifactIdentity identity, ArtifactStorage artifactStorage, Map<String, String> deploymentProperties,
        String repositoryName) throws DeploymentException {
        if (ArtifactIdentityDeterminer.BUNDLE_TYPE.equalsIgnoreCase(identity.getType())) {
            BundleInstallArtifact bundleInstallArtifact = this.bundleArtifactFactory.createBundleInstallArtifact(identity, artifactStorage, repositoryName);

            if (deploymentProperties != null) {
                bundleInstallArtifact.getDeploymentProperties().putAll(deploymentProperties);
            }

            return constructAssociatedGraphNode(bundleInstallArtifact);
        } else {
            return null;
        }
    }
}
