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

package org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.util.common.GraphNode;
import org.osgi.framework.BundleException;

/**
 * {@link CommitStage} is a {@link PipelineStage} which commits the changes in the side state.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class CommitStage implements PipelineStage {
    
    /**
     * {@inheritDoc}
     */
    public void process(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException {
        try {
            installEnvironment.getQuasiFramework().commit();
        } catch (BundleException e) {
            throw new DeploymentException("commit failed", e);
        }
    }

}
