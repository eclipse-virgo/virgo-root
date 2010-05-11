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
import org.eclipse.virgo.util.common.Tree;


/**
 * A <code>ScopedPlanInstallArtifactProcessor</code> is called by a
 * {@link ScopedPlanIdentifyingTreeVisitor} for each scoped plan that
 * it finds.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations <strong>must</strong> be thread-safe.
 *
 */
interface ScopedPlanInstallArtifactProcessor {
    
    /**
     * Process the supplied <code>plan</code>
     * @param plan the plan to process
     * @throws DeploymentException if a failure occurs during plan processing
     */
    void processScopedPlanInstallArtifact(Tree<InstallArtifact> plan) throws DeploymentException;
}
