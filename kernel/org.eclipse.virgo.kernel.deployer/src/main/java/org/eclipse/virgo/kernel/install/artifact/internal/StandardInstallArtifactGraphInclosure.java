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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphFactory;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphInclosure;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ArtifactIdentityScoper;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.GraphNode;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StandardInstallArtifactGraphInclosure} is a default implementation of {@link InstallArtifactGraphInclosure}
 * that can create with bundles, configuration files, and plans.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class StandardInstallArtifactGraphInclosure implements InstallArtifactGraphInclosure {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EventLogger eventLogger;

    private final BundleContext bundleContext;

    private final ArtifactStorageFactory artifactStorageFactory;

    public StandardInstallArtifactGraphInclosure(@NonNull ArtifactStorageFactory artifactStorageFactory, @NonNull BundleContext bundleContext,
        @NonNull EventLogger eventLogger) {
        this.artifactStorageFactory = artifactStorageFactory;
        this.eventLogger = eventLogger;
        this.bundleContext = bundleContext;
    }

    public GraphNode<InstallArtifact> constructGraphNode(ArtifactIdentity identity, File artifact, Map<String, String> properties,
        String repositoryName) throws DeploymentException {
        if (!artifact.exists()) {
            throw new DeploymentException(artifact + " does not exist");
        }
        ArtifactIdentity scopedIdentity = ArtifactIdentityScoper.scopeArtifactIdentity(identity);
        ArtifactStorage artifactStorage = this.artifactStorageFactory.create(artifact, scopedIdentity);
        try {
            return constructInstallArtifactGraph(scopedIdentity, properties, artifactStorage, repositoryName);
        } catch (DeploymentException e) {
            artifactStorage.delete();
            throw e;
        }
    }

    private GraphNode<InstallArtifact> constructInstallArtifactGraph(ArtifactIdentity identity, Map<String, String> deploymentProperties,
        ArtifactStorage artifactStorage, String repositoryName) throws DeploymentException {
        GraphNode<InstallArtifact> graph = null;
        List<OsgiServiceHolder<InstallArtifactGraphFactory>> iatfHolders = OsgiFrameworkUtils.getServices(this.bundleContext,
            InstallArtifactGraphFactory.class);

        for (OsgiServiceHolder<InstallArtifactGraphFactory> iatfHolder : iatfHolders) {

            InstallArtifactGraphFactory iatf = iatfHolder.getService();
            try {
                if (iatf != null) {
                    graph = iatf.constructInstallArtifactGraph(identity, artifactStorage, deploymentProperties, repositoryName);
                    if (graph != null) {
                        break;
                    }
                }
            } finally {
                this.bundleContext.ungetService(iatfHolder.getServiceReference());
            }
        }

        if (graph == null) {
            this.eventLogger.log(DeployerLogEvents.MISSING_ARTIFACT_FACTORY, identity.getType(), identity.getName(), identity.getVersion());
            throw new DeploymentException("Cannot create InstallArtifact for '" + identity + "'");
        }
        return graph;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphNode<InstallArtifact> recoverInstallGraph(ArtifactIdentity artifactIdentity, File artifact) {
        ArtifactStorage artifactStorage = null;
        try {
            artifactStorage = this.artifactStorageFactory.create(artifact, artifactIdentity);
            GraphNode<InstallArtifact> installArtifactGraph = constructInstallArtifactGraph(artifactIdentity, null, artifactStorage, null);

            return installArtifactGraph;
        } catch (RuntimeException e) {
            if (artifactStorage != null) {
                artifactStorage.delete();
            }
            this.logger.error(String.format("An error occurred during recovery of artefact '%s'", artifact), e);
            throw e;
        } catch (DeploymentException e) {
            if (artifactStorage != null) {
                artifactStorage.delete();
            }
            this.logger.warn(String.format("An error occurred during recovery of artefact '%s'", artifact), e);
            return null;
        }
    }

}
