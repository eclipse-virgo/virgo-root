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

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.deployer.core.DeployerLogEvents;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentOptions;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeInclosure;
import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ArtifactIdentityScoper;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.osgi.VersionRange;

/**
 * {@link StandardInstallArtifactTreeInclosure} is a default implementation of {@link InstallArtifactTreeInclosure} that
 * can create with bundles, configuration files, and plans.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class StandardInstallArtifactTreeInclosure implements InstallArtifactTreeInclosure {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EventLogger eventLogger;

    private final BundleContext bundleContext;

    private final Repository repository;

    private final ArtifactStorageFactory artifactStorageFactory;

    private final ArtifactIdentityDeterminer artifactIdentityDeterminer;

    public StandardInstallArtifactTreeInclosure(@NonNull ArtifactStorageFactory artifactStorageFactory, @NonNull BundleContext bundleContext,
        @NonNull Repository repository, @NonNull EventLogger eventLogger, @NonNull ArtifactIdentityDeterminer artifactIdentityDeterminer) {
        this.repository = repository;
        this.artifactStorageFactory = artifactStorageFactory;
        this.eventLogger = eventLogger;
        this.bundleContext = bundleContext;
        this.artifactIdentityDeterminer = artifactIdentityDeterminer;
    }

    /**
     * {@inheritDoc}
     */
    public Tree<InstallArtifact> createInstallTree(ArtifactSpecification specification) throws DeploymentException {
        return createInstallTree(specification, null);
    }

    /**
     * {@inheritDoc}
     */
    public Tree<InstallArtifact> createInstallTree(ArtifactSpecification specification, String scopeName) throws DeploymentException {
        String type = specification.getType();
        String name = specification.getName();
        VersionRange versionRange = specification.getVersionRange();
        RepositoryAwareArtifactDescriptor artifactDescriptor = this.repository.get(type, name, versionRange);
        if (artifactDescriptor == null) {
            this.eventLogger.log(DeployerLogEvents.ARTIFACT_NOT_FOUND, type, name, versionRange, this.repository.getName());
            throw new DeploymentException(type + " '" + name + "' version '" + versionRange + "' not found");
        }

        URI artifactURI = artifactDescriptor.getUri();
        
        ArtifactIdentity identity = new ArtifactIdentity(type, name, artifactDescriptor.getVersion(), scopeName);
        identity = ArtifactIdentityScoper.scopeArtifactIdentity(identity);       
        
        ArtifactStorage artifactStorage = this.artifactStorageFactory.create(new File(artifactURI), identity);

        Tree<InstallArtifact> installArtifactTree = constructInstallArtifactTree(identity, specification.getProperties(), artifactStorage,
            artifactDescriptor.getRepositoryName());
        return installArtifactTree;
    }

    /**
     * {@inheritDoc}
     */
    public Tree<InstallArtifact> createInstallTree(File sourceFile) throws DeploymentException {

        if (!sourceFile.exists()) {
            throw new DeploymentException(sourceFile + " does not exist");
        }

        ArtifactStorage artifactStorage = null;
        try {
            ArtifactIdentity artifactIdentity = determineIdentity(sourceFile);
            artifactStorage = this.artifactStorageFactory.create(sourceFile, artifactIdentity);
            Tree<InstallArtifact> installArtifactTree = constructInstallArtifactTree(artifactIdentity, null, artifactStorage, null);

            return installArtifactTree;
        } catch (DeploymentException e) {
            if (artifactStorage != null) {
              artifactStorage.delete();
            }
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage(), e);
        }
    }

    private ArtifactIdentity determineIdentity(File file) throws DeploymentException {
        ArtifactIdentity artifactIdentity = this.artifactIdentityDeterminer.determineIdentity(file, null);

        if (artifactIdentity == null) {
            this.eventLogger.log(DeployerLogEvents.INDETERMINATE_ARTIFACT_TYPE, file);
            throw new DeploymentException("Cannot determine the artifact identity of the file '" + file + "'");
        }

        return artifactIdentity;
    }

    private Tree<InstallArtifact> constructInstallArtifactTree(ArtifactIdentity identity, Map<String, String> deploymentProperties,
        ArtifactStorage artifactStorage, String repositoryName) throws DeploymentException {
        Tree<InstallArtifact> tree = null;
        List<OsgiServiceHolder<InstallArtifactTreeFactory>> iatfHolders = OsgiFrameworkUtils.getServices(this.bundleContext,
            InstallArtifactTreeFactory.class);

        for (OsgiServiceHolder<InstallArtifactTreeFactory> iatfHolder : iatfHolders) {

            InstallArtifactTreeFactory iatf = iatfHolder.getService();
            try {
                if (iatf != null) {
                    tree = iatf.constructInstallArtifactTree(identity, artifactStorage, deploymentProperties, repositoryName);
                    if (tree != null) {
                        break;
                    }
                }
            } finally {
                this.bundleContext.ungetService(iatfHolder.getServiceReference());
            }
        }

        if (tree == null) {
            this.eventLogger.log(DeployerLogEvents.MISSING_ARTIFACT_FACTORY, identity.getType(), identity.getName(), identity.getVersion());
            throw new DeploymentException("Cannot create InstallArtifact for '" + identity + "'");
        }
        return tree;
    }

    /**
     * {@inheritDoc}
     */
    public Tree<InstallArtifact> recoverInstallTree(File sourceFile, DeploymentOptions deploymentOptions) {
        ArtifactStorage artifactStorage = null;
        if (deploymentOptions.getRecoverable() && (!deploymentOptions.getDeployerOwned() || sourceFile.exists())) {
            try {
                ArtifactIdentity artifactIdentity = determineIdentity(sourceFile);
                artifactStorage = this.artifactStorageFactory.create(sourceFile, artifactIdentity);
                Tree<InstallArtifact> installArtifactTree = constructInstallArtifactTree(artifactIdentity, null, artifactStorage, null);

                return installArtifactTree;
            } catch (RuntimeException e) {
                if (artifactStorage != null) {
                    artifactStorage.delete();
                }
                this.logger.error(String.format("An error occurred during recovery of artefact '%s'", sourceFile), e);
                throw e;
            } catch (DeploymentException e) {
                artifactStorage.delete();
                this.logger.warn(String.format("An error occurred during recovery of artefact '%s'", sourceFile), e);
                return null;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void updateStagingArea(File sourceFile, ArtifactIdentity identity) throws DeploymentException {
        this.artifactStorageFactory.create(sourceFile, identity).synchronize();
    }
}
