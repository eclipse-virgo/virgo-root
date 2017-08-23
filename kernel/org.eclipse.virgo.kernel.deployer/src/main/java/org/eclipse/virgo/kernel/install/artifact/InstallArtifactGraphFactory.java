/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution (InstallArtifactTreeFactory)
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact;

import java.util.Map;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * {@link InstallArtifactGraphFactory} is used to create graphs of {@link InstallArtifact InstallArtifacts}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface InstallArtifactGraphFactory {

    /**
     * Constructs an install graph from the {@link ArtifactStorage}. If this factory cannot handle the given artifact type, it
     * returns <code>null</code>.
     * @param artifactIdentity 
     * @param artifactStorage 
     * @param deploymentProperties the deployment properties for the artifact. Can be <code>null</code>.
     * @param repositoryName The name of the repository from which that artifact originates, or <code>null</code> if the artifact is not from a repository.
     * @return an install graph or <code>null</code> if the factory cannot handle the given artifact type
     * @throws DeploymentException if the graph cannot be constructed
     */
    GraphNode<InstallArtifact> constructInstallArtifactGraph(ArtifactIdentity artifactIdentity, ArtifactStorage artifactStorage, Map<String, String> deploymentProperties,
        String repositoryName) throws DeploymentException;

}
