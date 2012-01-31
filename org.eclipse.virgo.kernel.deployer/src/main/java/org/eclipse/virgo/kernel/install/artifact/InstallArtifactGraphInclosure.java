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

package org.eclipse.virgo.kernel.install.artifact;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentOptions;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * {@link InstallArtifactGraphInclosure} is used to create, and store persistently, various types of
 * {@link InstallArtifact}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public interface InstallArtifactGraphInclosure {

    /**
     * Looks up the artifact with the given specification in the repository.
     * <p />
     * Precondition specification.getUri() == null
     * 
     * @param specification the specification of the artifact
     * @return a {@linkRepositoryAwareArtifactDescriptor}
     * @throws DeploymentException if the artifact is not found in the repository
     */
    RepositoryAwareArtifactDescriptor lookup(ArtifactSpecification specification) throws DeploymentException;

    /**
     * Determines the identity of the artifact at the given URI with the given scope name.
     * 
     * @param uri the URI of the artifact
     * @param scopeName the scope name
     * @return an {@link ArtifactIdentity}
     * @throws DeploymentException if the identity cannot be determined
     */
    ArtifactIdentity determineIdentity(@NonNull URI uri, String scopeName) throws DeploymentException;

    /**
     * Create an install graph consisting of a single artifact with the given identity and file contents, the given
     * artifact properties, optionally originating from the repository with the given name.
     * 
     * @param identity the identity of the artifact
     * @param artifact the file contents of the artifact
     * @param properties the artifact properties
     * @param repositoryName the name of the repository from which the artifact originated or <code>null</code> if the
     *        artifact did not originate from a repository
     * @return an install graph
     * @throws DeploymentException if the graph cannot be created
     */
    GraphNode<InstallArtifact> constructGraphNode(ArtifactIdentity identity, File artifact, Map<String, String> properties, String repositoryName)
        throws DeploymentException;

    /**
     * Optionally recover an install graph from the staging area using the given file URI to identify the artifact. The
     * source URI need no longer be valid unless the artifact is owned by the deployer. Non-recoverable artifacts are
     * "recovered" by deleting them.
     * 
     * @param location the source location of the artifact
     * @param options the {@link DeploymentOptions} of the artifact
     * @return an install graph or <code>null</code>
     */
    GraphNode<InstallArtifact> recoverInstallGraph(File location, DeploymentOptions options);

    /**
     * Update the copy of the given artefact in the deploy area.
     * 
     * @param sourceLocation the location of the artefact to be updated
     * @param identity the identity of the artifact to be updated
     * @throws DeploymentException
     */
    void updateStagingArea(File sourceLocation, ArtifactIdentity identity) throws DeploymentException;

}
