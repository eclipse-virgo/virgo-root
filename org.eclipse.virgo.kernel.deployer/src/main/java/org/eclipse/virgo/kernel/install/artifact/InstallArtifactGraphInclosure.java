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
import java.util.Map;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
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
    GraphNode<InstallArtifact> recoverInstallGraph(ArtifactIdentity identity, File location);

}
