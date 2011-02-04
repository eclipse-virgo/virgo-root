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

package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironmentFactory;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.util.common.Tree;


/**
 * A helper class for handling refresh of an {@link InstallArtifact}. When an <code>InstallArtifact</code>
 * is refreshed, its tree is passed through the refresh {@link Pipeline}.

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
        Tree<InstallArtifact> tree = installArtifact.getTree();
        
        boolean refreshed = true;
        InstallEnvironment installEnvironment = this.installEnvironmentFactory.createInstallEnvironment(installArtifact);
        try {
            this.refreshPipeline.process(tree, installEnvironment);
        } catch (UnableToSatisfyBundleDependenciesException _) {
            refreshed = false;
        } catch (DeploymentException _) {
            refreshed = false;
        } finally {
            installEnvironment.destroy();
        }
        return refreshed;
    }
}
