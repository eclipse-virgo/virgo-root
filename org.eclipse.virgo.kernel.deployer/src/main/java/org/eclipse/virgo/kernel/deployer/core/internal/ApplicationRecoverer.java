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

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.net.URI;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;


/**
 * {@link ApplicationRecoverer} is an interface to an {@link org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer ApplicationDeployer} 
 * intended only for use by the recovery agent.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations of this interface must be thread safe.
 *
 */
public interface ApplicationRecoverer {

    /**
     * Redeploy the artifact that was originally deployed from the given URI.
     * 
     * @param location URI of artifact
     * @param options the {@link DeploymentOptions} of the artifact
     * @throws DeploymentException 
     */
    void recoverDeployment(URI location, DeploymentOptions options) throws DeploymentException;

}
