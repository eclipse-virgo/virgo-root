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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;


import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.common.Tree.ExceptionThrowingTreeVisitor;

final class ScopedPlanIdentifyingTreeVisitor implements ExceptionThrowingTreeVisitor<InstallArtifact, DeploymentException> {

    private final ScopedPlanInstallArtifactProcessor planProcessor;
    
    ScopedPlanIdentifyingTreeVisitor(ScopedPlanInstallArtifactProcessor planProcessor) {

        this.planProcessor = planProcessor;
    }

    public boolean visit(Tree<InstallArtifact> tree) throws DeploymentException {
        if (isScopedPlan(tree.getValue())) {
            this.planProcessor.processScopedPlanInstallArtifact(tree);                
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
