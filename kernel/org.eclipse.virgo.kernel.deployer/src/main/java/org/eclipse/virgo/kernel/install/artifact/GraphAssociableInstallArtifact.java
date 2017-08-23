/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * {@link GraphAssociableInstallArtifact} is an {@link InstallArtifact} which may have its graph node set.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementation <strong>must</strong> be thread-safe.
 */
public interface GraphAssociableInstallArtifact extends InstallArtifact {

    /**
     * Associate the given graph with this install artifact.
     * 
     * @param graph to set
     * @throws DeploymentException possible from overriding methods
     */
    void setGraph(GraphNode<InstallArtifact> graph) throws DeploymentException;

}
