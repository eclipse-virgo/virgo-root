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
import org.eclipse.virgo.kernel.install.artifact.internal.StandardPlanInstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.util.common.Tree;


/**
 * A {@link Transformer} implementation that is responsible for scoping
 * artifacts in an install tree.
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
    public void transform(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException {
        ScopedPlanIdentifyingTreeVisitor planIdentifyingVisitor = new ScopedPlanIdentifyingTreeVisitor(this);
        installTree.visit(planIdentifyingVisitor);
    }

    /** 
     * {@inheritDoc}
     */
    public void processScopedPlanInstallArtifact(Tree<InstallArtifact> planTree) throws DeploymentException {
        InstallArtifact value = planTree.getValue();
        if (value instanceof StandardPlanInstallArtifact) {
            ((StandardPlanInstallArtifact)value).scope();
        }
    }
}
