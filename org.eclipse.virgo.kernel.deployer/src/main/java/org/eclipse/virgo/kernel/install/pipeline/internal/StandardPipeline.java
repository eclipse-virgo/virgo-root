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

package org.eclipse.virgo.kernel.install.pipeline.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.install.pipeline.stage.AbstractPipelineStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * {@link StandardPipeline} is the default implementation of {@link Pipeline}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
class StandardPipeline extends AbstractPipelineStage implements Pipeline {

    private final Object monitor = new Object();

    private final List<PipelineStage> stageList = new ArrayList<PipelineStage>();

    /**
     * {@inheritDoc}
     */
    public Pipeline appendStage(PipelineStage stage) {
        synchronized (this.monitor) {
            this.stageList.add(stage);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        for (int i = 0; i < numStages(); i++) {
            PipelineStage nextStage;
            synchronized (this.monitor) {
                nextStage = this.stageList.get(i);
            }
            nextStage.process(installGraph, installEnvironment);
        }
    }

    private int numStages() {
        synchronized (this.monitor) {
            return this.stageList.size();
        }
    }

}
