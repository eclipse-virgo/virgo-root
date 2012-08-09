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

package org.eclipse.virgo.kernel.install.artifact;

import java.util.List;

import org.eclipse.virgo.kernel.artifact.ArtifactSpecification;
import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;


/**
 * {@link PlanInstallArtifact} is an {@link InstallArtifact} for a plan.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public interface PlanInstallArtifact extends GraphAssociableInstallArtifact {

    /**
     * Returns whether or not this plan is scoped.
     * 
     * @return <code>true</code> if and only if this plan is scoped
     */
    boolean isScoped();

    /**
     * Returns whether or not this plan is atomic.
     * 
     * @return <code>true</code> if and only if this plan is atomic
     */
    boolean isAtomic();

    /**
     * Returns {@link ArtifactSpecification}s for the artifacts of this plan.
     * 
     * @return a list of <code>ArtifactSpecification</code>s
     */
    public List<ArtifactSpecification> getArtifactSpecifications();

    /**
     * Refresh the child of this plan with the given symbolic name.
     * 
     * @param symbolicName the symbolic name of the child to refresh
     * @return <code>true</code> if and only if the child was successfully refreshed
     * @throws DeploymentException if an error occurred during refresh
     */
    boolean refresh(String symbolicName) throws DeploymentException;

    /**
     * If this plan is scoped, run its install tree through the refresh subpipeline. If this plan is unscoped, recurse
     * to any parents this plan has. Note that a plan may be a descendent of at most one scoped plan since scopes do not
     * overlap.
     * 
     * @return <code>true</code> if successfully refreshed, <code>false</code> otherwise
     */
    boolean refreshScope();

    /**
     * Returns the {@link PlanDescriptor.Provisioning} for the plan.
     * @return the provisioning setting of the plan
     */
    PlanDescriptor.Provisioning getProvisioning();

}
