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
import org.eclipse.virgo.util.common.GraphNode;


/**
 * A <code>ScopedPlanInstallArtifactProcessor</code> is called by a
 * {@link ScopedPlanIdentifyingDirectedAcyclicGraphVisitor} for each scoped plan that
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
    void processScopedPlanInstallArtifact(GraphNode<InstallArtifact> plan) throws DeploymentException;
}
