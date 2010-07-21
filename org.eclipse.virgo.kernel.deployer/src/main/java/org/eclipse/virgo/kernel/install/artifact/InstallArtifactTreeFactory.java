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

import java.util.Map;


import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.util.common.Tree;

/**
 * {@link InstallArtifactTreeFactory} is used to create trees of {@link InstallArtifact InstallArtifacts}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface InstallArtifactTreeFactory {

    /**
     * Constructs an install tree from the {@link ArtifactStorage}. If this factory cannot handle the given artifact type, it
     * returns <code>null</code>.
     * @param artifactIdentity 
     * @param artifactStorage 
     * @param deploymentProperties the deployment properties for the artifact. Can be <code>null</code>.
     * @param repositoryName The name of the repository from which that artifact originates, or <code>null</code> if the artifact is not from a repository.
     * @return an install tree or <code>null</code> if the factory cannot handle the given artifact type
     * @throws DeploymentException if the tree cannot be constructed
     */
    Tree<InstallArtifact> constructInstallArtifactTree(ArtifactIdentity artifactIdentity, ArtifactStorage artifactStorage, Map<String, String> deploymentProperties,
        String repositoryName) throws DeploymentException;

}
