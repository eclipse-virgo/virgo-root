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

package org.eclipse.virgo.kernel.install.pipeline.stage;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * {@link AbstractPipelineStage} is a common base class for {@link PipelineStage} implementations.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public abstract class AbstractPipelineStage implements PipelineStage {

    /**
     * {@inheritDoc}
     */
    public final void process(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        InstallLog installLog = installEnvironment.getInstallLog();
        installLog.log(this, "process entry with installGraph '%s'", installGraph.toString());
        try {
            doProcessGraph(installGraph, installEnvironment);
        } catch (DeploymentException de) {
            installLog.log(this, "process exit with installGraph '%s', exception '%s' thrown", installGraph.toString(), de.toString());
            throw de;
        } catch (UnableToSatisfyBundleDependenciesException utsbde) {
            installLog.log(this, "process exit with installGraph '%s', exception '%s' thrown", installGraph.toString(), utsbde.toString());
            throw utsbde;
        } catch (RuntimeException re) {
            installLog.log(this, "process exit with installGraph '%s', exception '%s' thrown", installGraph.toString(), re.toString());
            throw re;
        } 
        installLog.log(this, "process exit with installGraph '%s'", installGraph.toString());
    }

    /**
     * Processes the given install graph in the context of the given {@link InstallEnvironment}. The default
     * implementation simply calls the <code>doProcessNode</code> method for each node in the graph. If a different
     * behaviour is required, the subclass should override this method.
     * 
     * @param installGraph the graph to be processed
     * @param installEnvironment the <code>InstallEnvironment</code> in the context of which to do the processing
     * @throws {@link UnableToSatisfyBundleDependenciesException} if a bundle's dependencies cannot be satisfied
     * @throws DeploymentException if a failure occurs
     */
    protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException,
        UnableToSatisfyBundleDependenciesException {
        InstallArtifact value = installGraph.getValue();
        doProcessNode(value, installEnvironment);
        for (GraphNode<InstallArtifact> child : installGraph.getChildren()) {
            doProcessGraph(child, installEnvironment);
        }
    }

    /**
     * Processes the given {@link InstallArtifact} in the context of the given {@link InstallEnvironment}. Subclasses
     * should override this method if they do not override the doProcessGraph method.
     * 
     * @param installArtifact the graph node to be processed
     * @param installEnvironment the <code>InstallEnvironment</code> in the context of which to do the processing
     * @throws DeploymentException if a failure occurs
     */
    protected void doProcessNode(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException {
    }

}
