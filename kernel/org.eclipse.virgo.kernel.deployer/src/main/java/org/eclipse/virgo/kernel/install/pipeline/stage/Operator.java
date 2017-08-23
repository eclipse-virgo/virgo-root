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

package org.eclipse.virgo.kernel.install.pipeline.stage;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;


/**
 * {@link Operator} is a kernel extension interface used to perform point-wise operations on install tree nodes.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class must be thread safe.
 * 
 */
public interface Operator {

    /**
     * Operates on the given {@link InstallArtifact}.
     * 
     * @param installArtifact the <code>InstallArtifact</code> to normalize
     * @param installEnvironment the <code>InstallEnvironment</code>
     * @throws DeploymentException if the operation fails
     */
    void operate(InstallArtifact installArtifact, InstallEnvironment installEnvironment) throws DeploymentException;

}
