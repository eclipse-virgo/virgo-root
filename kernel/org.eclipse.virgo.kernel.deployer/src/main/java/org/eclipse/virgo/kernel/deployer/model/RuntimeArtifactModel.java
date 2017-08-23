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

package org.eclipse.virgo.kernel.deployer.model;

import java.net.URI;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;


/**
 * {@link RuntimeArtifactModel} tracks all the {@link InstallArtifact InstallArtifacts} in the kernel.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface RuntimeArtifactModel {

    /**
     * Adds the given {@link InstallArtifact} deployed from the given {@link URI} to this {@link RuntimeArtifactModel}.
     * 
     * @param location the <code>URI</code> from which the artifact was deployed
     * @param installArtifact the <code>InstallArtifact</code>
     * @return the {@link DeploymentIdentity} of the <code>InstallArtifact</code>
     * @throws DuplicateFileNameException 
     * @throws DuplicateLocationException 
     * @throws DuplicateDeploymentIdentityException 
     * @throws DeploymentException 
     */
    DeploymentIdentity add(URI location, InstallArtifact installArtifact) throws DuplicateFileNameException, DuplicateLocationException,
        DuplicateDeploymentIdentityException, DeploymentException;

    /**
     * Gets the {@link InstallArtifact} with the given {@link DeploymentIdentity} in this {@link RuntimeArtifactModel}.
     * If there is no such <code>InstallArtifact</code>, returns <code>null</code>.
     * 
     * @param deploymentIdentity the <code>DeploymentIdentity</code> of the <code>InstallArtifact</code> to get
     * @return the <code>InstallArtifact</code> or <code>null</code> if there is no such <code>InstallArtifact</code>
     */
    InstallArtifact get(DeploymentIdentity deploymentIdentity);

    /**
     * Gets the {@link InstallArtifact} deployed from the given {@link URI} in this {@link RuntimeArtifactModel}. If
     * there is no such <code>InstallArtifact</code>, returns <code>null</code>.
     * 
     * @param location the <code>URI</code> of the <code>InstallArtifact</code> to get
     * @return the <code>InstallArtifact</code> or <code>null</code> if there is no such <code>InstallArtifact</code>
     */
    InstallArtifact get(URI location);

    /**
     * Gets the {@link URI} from which the artifact with the given {@link DeploymentIdentity} was deployed.
     * 
     * @param deploymentIdentity the <code>DeploymentIdentity</code> of the artifact
     * @return the <code>URI</code> from which the artifact was deployed, or <code>null</code> if no such artifact was
     *         found
     */
    URI getLocation(DeploymentIdentity deploymentIdentity);

    /**
     * Gets an array of all the {@link DeploymentIdentity DeploymentIdentities} in this {@link RuntimeArtifactModel}.
     * 
     * @return an array of <code>DeploymentIdentity</code>
     */
    DeploymentIdentity[] getDeploymentIdentities();

    /**
     * Deletes the {@link InstallArtifact} with the given {@link DeploymentIdentity} from this
     * {@link RuntimeArtifactModel} and returns the <code>InstallArtifact</code>. If no such artifact is present, does
     * not modify this <code>RuntimeArtifactModel</code> and returns <code>null</code>.
     * 
     * @param deploymentIdentity the <code>DeploymentIdentity</code> of the artifact to be deleted
     * @return the <code>InstallArtifact</code> which was deleted
     * @throws DeploymentException 
     */
    InstallArtifact delete(DeploymentIdentity deploymentIdentity) throws DeploymentException;

}
