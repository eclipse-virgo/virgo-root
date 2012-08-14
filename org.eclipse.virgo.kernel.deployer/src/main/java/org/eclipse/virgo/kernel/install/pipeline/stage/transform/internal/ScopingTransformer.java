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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;


import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.StandardPlanInstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.util.common.GraphNode;


/**
 * A {@link Transformer} implementation that is responsible for scoping
 * artifacts in an install graph.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public class ScopingTransformer implements Transformer, ScopedPlanInstallArtifactProcessor {

    /** 
     * {@inheritDoc}
     */
    public void transform(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException {
        ScopedPlanIdentifyingDirectedAcyclicGraphVisitor planIdentifyingVisitor = new ScopedPlanIdentifyingDirectedAcyclicGraphVisitor(this);
        installGraph.visit(planIdentifyingVisitor);
    }

    /** 
     * {@inheritDoc}
     */
    public void processScopedPlanInstallArtifact(GraphNode<InstallArtifact> planGraph) throws DeploymentException {
        InstallArtifact value = planGraph.getValue();
        if (value instanceof StandardPlanInstallArtifact) {
            ((StandardPlanInstallArtifact)value).scope();
        }
    }
}
