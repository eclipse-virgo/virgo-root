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

package org.eclipse.virgo.kernel.model.management.internal;

import java.net.URI;

import javax.management.ObjectName;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.model.management.InstallException;
import org.eclipse.virgo.kernel.model.management.Installer;
import org.eclipse.virgo.kernel.model.management.RuntimeArtifactModelObjectNameCreator;
import org.osgi.framework.Version;


/**
 * Implementation of {@link Installer} that delegates to the deployer
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class DelegatingInstaller implements Installer {

    private final ApplicationDeployer deployer;

    private final RuntimeArtifactModelObjectNameCreator artifactObjectNameCreator;

    public DelegatingInstaller(ApplicationDeployer deployer, RuntimeArtifactModelObjectNameCreator artifactObjectNameCreator) {
        this.deployer = deployer;
        this.artifactObjectNameCreator = artifactObjectNameCreator;
    }

    public ObjectName install(String uri) throws InstallException {
        DeploymentIdentity deploymentIdentity;
        try {
            deploymentIdentity = this.deployer.install(URI.create(uri));
        } catch (DeploymentException e) {
            throw new InstallException(String.format("Exception encountered while installing '%s'", uri), e);
        }
        return this.artifactObjectNameCreator.createModel(deploymentIdentity.getType(), deploymentIdentity.getSymbolicName(), new Version(deploymentIdentity.getVersion()));
    }

}
