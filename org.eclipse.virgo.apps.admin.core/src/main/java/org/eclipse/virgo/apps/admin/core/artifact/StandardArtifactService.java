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

package org.eclipse.virgo.apps.admin.core.artifact;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.apps.admin.core.ArtifactService;
import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.services.work.WorkArea;

/**
 * Standard implementation of {@link ArtifactService}.
 * 
 */
final class StandardArtifactService implements ArtifactService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardArtifactService.class);

    private final ApplicationDeployer applicationDeployer;

    private final File stagingDir;

    /**
     * @param applicationDeployer
     * @param subsystemRegistry
     * @param workAreaManager
     * @param bundleContext
     * @param serverApplicationInfoSource
     */
    StandardArtifactService(ApplicationDeployer applicationDeployer, WorkArea workArea) {
        this.applicationDeployer = applicationDeployer;
        this.stagingDir = workArea.getWorkDirectory().newChild("upload").createDirectory().toFile();
    }

    /**
     * {@inheritDoc}
     */
    public String deploy(File stagedFile) {
        try {
            this.applicationDeployer.deploy(stagedFile.toURI());
            return "Artifact deployed";
        } catch (DeploymentException e) {
            if (!stagedFile.delete()) {
                LOGGER.warn(String.format("Failed to delete %s after a deployment exception.", stagedFile));
            }
            return String.format("Deployment Error '%s'", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public File getStagingDirectory() {
        return this.stagingDir;
    }

}
