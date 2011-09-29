/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.model.internal.deployer;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.osgi.framework.BundleContext;


/**
 * {@link DeployerConfigArtifact} is a {@link DeployerArtifact} that understands
 * a configuration install artifact's properties.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class DeployerConfigArtifact extends DeployerArtifact {

    private final InstallArtifact installArtifact;

    public DeployerConfigArtifact(BundleContext bundleContext, InstallArtifact installArtifact, Region region) {
        super(bundleContext, installArtifact, region);
        this.installArtifact = installArtifact;
    }

   
}
