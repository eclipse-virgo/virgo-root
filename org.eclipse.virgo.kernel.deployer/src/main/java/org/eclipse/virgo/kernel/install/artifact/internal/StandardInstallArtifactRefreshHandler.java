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

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironmentFactory;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.util.common.GraphNode;


/**
 * A helper class for handling refresh of an {@link InstallArtifact}. When an <code>InstallArtifact</code>
 * is refreshed, its graph is passed through the refresh {@link Pipeline}.

 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe
 *
 */
final class StandardInstallArtifactRefreshHandler implements InstallArtifactRefreshHandler {
    
    private final InstallEnvironmentFactory installEnvironmentFactory;
    
    private final Pipeline refreshPipeline;

    StandardInstallArtifactRefreshHandler(InstallEnvironmentFactory installEnvironmentFactory, Pipeline refreshPipeline) {
        this.installEnvironmentFactory = installEnvironmentFactory;
        this.refreshPipeline = refreshPipeline;
    }
    
    public boolean refresh(InstallArtifact installArtifact) {
        GraphNode<InstallArtifact> graph = installArtifact.getGraph();
        
        boolean refreshed = true;
        InstallEnvironment installEnvironment = this.installEnvironmentFactory.createInstallEnvironment(installArtifact);
        try {
            this.refreshPipeline.process(graph, installEnvironment);
        } catch (UnableToSatisfyBundleDependenciesException utsbde) {
            refreshed = false;
        } catch (DeploymentException de) {
            refreshed = false;
        } finally {
            installEnvironment.destroy();
        }
        return refreshed;
    }
}
