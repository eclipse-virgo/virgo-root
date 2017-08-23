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

package org.eclipse.virgo.kernel.install.pipeline.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;

import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * {@link CompensatingPipeline} is a {@link Pipeline} which runs like any other pipeline but if one of its stages throws
 * an exception, it runs a compensation <code>PipelineStage</code> and then re-throws the exception.
 * <p />
 * For example, consider the following pipeline:
 * 
 * <pre>
 * P = beginX -&gt; X1 -&gt; X2 -&gt; X3 -&gt; endX
 * </pre>
 * 
 * and suppose we need to drive a failure event using a pipeline stage failX when any of X1, X2, X3 throw an exception.
 * Then we can use a <code>CompensatingPipeline</code> to produce the required behaviour:
 * 
 * <pre>
 * P' = beginX -&gt; CompensatingPipeline(X1 -&gt; X2 -&gt; X3, failX) -&gt; endX
 * </pre>
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class CompensatingPipeline extends StandardPipeline {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PipelineStage compensation;

    /**
     * Create a {@link CompensatingPipeline} with no pipeline stages and a given compensation stage.
     * @param compensation stage for compensation
     */
    public CompensatingPipeline(PipelineStage compensation) {
        super();
        this.compensation = compensation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        try {
            super.doProcessGraph(installGraph, installEnvironment);
        } catch (DeploymentException de) {
            compensate(installGraph, installEnvironment, de);
            throw de;
        } catch (UnableToSatisfyBundleDependenciesException utsbde) {
            compensate(installGraph, installEnvironment, utsbde);
            throw utsbde;
        } catch (RuntimeException re) {
            compensate(installGraph, installEnvironment, re);
            throw re;
        }
    }

    private void compensate(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment, Exception e) {
        try {
            if (!(e instanceof DeploymentException) || !((DeploymentException)e).isDiagnosed()) {
                installEnvironment.getInstallLog().logFailure(DeployerLogEvents.INSTALL_FAILURE, e);
            } else {
                installEnvironment.getInstallLog().logFailure(DeployerLogEvents.INSTALL_FAILURE, null);
            }
            this.compensation.process(installGraph, installEnvironment);
        } catch (Exception ex) {
            logger.warn(String.format("exception thrown while compensating for '%s'", e.getMessage()), ex);
        }
    }

}
