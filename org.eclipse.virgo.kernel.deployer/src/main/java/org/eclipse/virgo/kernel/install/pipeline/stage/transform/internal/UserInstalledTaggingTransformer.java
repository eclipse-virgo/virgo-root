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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;


import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.util.common.Tree;

/**
 * An implementation of {@link Transformer} that marks an {@link InstallArtifact} as having been user installed. This is
 * done by setting a property with a key of <code>user.installed</code> and a value of <code>true</code>.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public class UserInstalledTaggingTransformer implements Transformer {

    private static final String USER_INSTALLED = "user.installed";

    /**
     * {@inheritDoc}
     */
    public void transform(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) throws DeploymentException {
        installTree.getValue().setProperty(USER_INSTALLED, Boolean.TRUE.toString());
    }

}
