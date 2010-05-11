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

package org.eclipse.virgo.kernel.install.pipeline.stage.visit.internal;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.AbstractPipelineStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.visit.Visitor;
import org.eclipse.virgo.util.common.Tree;

/**
 * {@link VisitationStage} is is a pipeline stage that drives {@link Visitor Visitors}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class VisitationStage extends AbstractPipelineStage {

    private final Visitor visitation;

    private final boolean preOrder;

    public VisitationStage(Visitor visitation) {
        this(visitation, true);
    }

    public VisitationStage(Visitor visitation, boolean preOrder) {
        this.visitation = visitation;
        this.preOrder = preOrder;
    }

    /**
     * {@inheritDoc}
     */
    public void doProcessNode(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
        this.visitation.operate(installArtifact, installEnvironment);
    }

    @Override
    protected void doProcessTree(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        if (this.preOrder) {
            super.doProcessTree(installTree, installEnvironment);
        } else {
            // Traverse in postorder.
            InstallArtifact value = installTree.getValue();
            for (Tree<InstallArtifact> child : installTree.getChildren()) {
                doProcessTree(child, installEnvironment);
            }
            doProcessNode(value, installEnvironment);
        }
    }

}
