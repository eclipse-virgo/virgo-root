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

package org.eclipse.virgo.kernel.model.internal.deployer;

import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.model.CompositeArtifact;
import org.eclipse.equinox.region.Region;

/**
 * Implementation of {@link CompositeArtifact} that delegates to a Kernel {@link PlanInstallArtifact}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
final class DeployerCompositeArtifact extends DeployerArtifact implements CompositeArtifact {

    private final PlanInstallArtifact installArtifact;

    public DeployerCompositeArtifact(@NonNull BundleContext bundleContext, @NonNull PlanInstallArtifact installArtifact, @NonNull Region region) {
        super(bundleContext, installArtifact, region);
        this.installArtifact = installArtifact;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAtomic() {
        return this.installArtifact.isAtomic();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isScoped() {
        return this.installArtifact.isScoped();
    }

    /**
     * Gets the underlying {@link PlanInstallArtifact} encapsulated by this {@link DeployerCompositeArtifact}
     * 
     * @return The underlying {@link PlanInstallArtifact}
     */
    PlanInstallArtifact getInstallArtifact() {
        return this.installArtifact;
    }
}
