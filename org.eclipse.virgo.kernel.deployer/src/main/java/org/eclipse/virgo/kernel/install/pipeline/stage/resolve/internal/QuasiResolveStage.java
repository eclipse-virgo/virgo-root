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

package org.eclipse.virgo.kernel.install.pipeline.stage.resolve.internal;

import java.util.List;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.util.common.Tree;

/**
 * {@link QuasiResolveStage} is a {@link PipelineStage} which attempts to resolve the side state.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class QuasiResolveStage implements PipelineStage {

    /**
     * {@inheritDoc}
     */
    public void process(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException, UnableToSatisfyBundleDependenciesException {
        QuasiFramework quasiFramework = installEnvironment.getQuasiFramework();
        List<QuasiResolutionFailure> resolutionFailures = quasiFramework.resolve();
        if (!resolutionFailures.isEmpty()) {
            QuasiResolutionFailure failure = resolutionFailures.get(0);
            throw new UnableToSatisfyBundleDependenciesException(failure.getUnresolvedQuasiBundle().getSymbolicName(), failure.getUnresolvedQuasiBundle().getVersion(),
                failure.getDescription());
        }
    }

}
