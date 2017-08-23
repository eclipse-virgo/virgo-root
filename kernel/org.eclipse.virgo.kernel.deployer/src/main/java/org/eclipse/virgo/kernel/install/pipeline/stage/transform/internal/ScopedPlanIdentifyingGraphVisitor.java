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
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;

final class ScopedPlanIdentifyingDirectedAcyclicGraphVisitor implements ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, DeploymentException> {

    private final ScopedPlanInstallArtifactProcessor planProcessor;
    
    ScopedPlanIdentifyingDirectedAcyclicGraphVisitor(ScopedPlanInstallArtifactProcessor planProcessor) {

        this.planProcessor = planProcessor;
    }

    public boolean visit(GraphNode<InstallArtifact> graph) throws DeploymentException {
        if (isScopedPlan(graph.getValue())) {
            this.planProcessor.processScopedPlanInstallArtifact(graph);                
            return false;
        }
        return true;
    }

    private boolean isScopedPlan(InstallArtifact installArtifact) {
        boolean scopedPlan = false;

        if (installArtifact instanceof PlanInstallArtifact) {
            scopedPlan = ((PlanInstallArtifact) installArtifact).isScoped();
        }

        return scopedPlan;
    }
}
