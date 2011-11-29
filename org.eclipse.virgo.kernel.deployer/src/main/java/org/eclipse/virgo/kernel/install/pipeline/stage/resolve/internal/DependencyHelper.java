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

package org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal;

import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Dependencies;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.util.common.GraphNode;

final class DependencyHelper {
    
    static boolean dependenciesToBeInstalled(GraphNode<InstallArtifact> installGraph) {
        boolean installDependencies = true;
        InstallArtifact rootArtifact = installGraph.getValue();
        if (rootArtifact instanceof PlanInstallArtifact) {
            PlanInstallArtifact rootPlan = (PlanInstallArtifact) rootArtifact;
            installDependencies = Dependencies.INSTALL == rootPlan.getDependencies();
        }
        return installDependencies;
    }

}
