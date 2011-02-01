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

package org.eclipse.virgo.kernel.install.artifact;

import java.io.File;


import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentOptions;
import org.eclipse.virgo.util.common.Tree;

/**
 * {@link InstallArtifactTreeInclosure} is used to create, and store persistently, various types of
 * {@link InstallArtifact}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public interface InstallArtifactTreeInclosure {

    /**
     * Create an install tree consisting of the single artifact matching the supplied {@link ArtifactSpecification}.
     * 
     * @param artifactSpecification the <code>ArtifactSpecification</code>.
     * @return an install tree
     * @throws DeploymentException if the tree cannot be created
     */
    Tree<InstallArtifact> createInstallTree(ArtifactSpecification artifactSpecification) throws DeploymentException;

    /**
     * Create an install tree consisting of the single artifact matching the supplied {@link ArtifactSpecification}. if
     * the given scope name is non-<code>null</code>, the installation is scoped. If the given scope name is
     * <code>null</code>, the behaviour of this method is equivalent to that of the <code>createInstallTree</code>
     * method with no scope name parameter.
     * 
     * @param artifactSpecification the <code>ArtifactSpecification</code>.
     * @param scopeName the scope name of the artifact or <code>null</code> if it does not belong to a scope
     * @return an install tree
     * @throws DeploymentException if the tree cannot be created
     */
    Tree<InstallArtifact> createInstallTree(ArtifactSpecification artifactSpecification, String scopeName) throws DeploymentException;

    /**
     * Create an install tree consisting of the single artifact available at the given file URI.
     * 
     * @param location the artifact's location
     * @return an install tree
     * @throws DeploymentException if the tree cannot be created
     */
    Tree<InstallArtifact> createInstallTree(File location) throws DeploymentException;

    /**
     * Optionally recover an install tree from the staging area using the given file URI to identify the artifact. The
     * source URI need no longer be valid unless the artifact is owned by the deployer. Non-recoverable artifacts are
     * "recovered" by deleting them.
     * 
     * @param location the source location of the artifact
     * @param options the {@link DeploymentOptions} of the artifact
     * @return an install tree or <code>null</code>
     */
    Tree<InstallArtifact> recoverInstallTree(File location, DeploymentOptions options);

    /**
     * Update the copy of the given artefact in the deploy area.
     * 
     * @param sourceLocation the location of the artefact to be updated
     * @param identity the identity of the artifact to be updated
     * @throws DeploymentException
     */
    void updateStagingArea(File sourceLocation, ArtifactIdentity identity) throws DeploymentException;

}
