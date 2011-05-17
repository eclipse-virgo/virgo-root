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

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.model.BundleArtifact;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.osgi.framework.BundleContext;

final class DeployerBundleArtifact extends DeployerArtifact implements BundleArtifact {

    private final BundleInstallArtifact installArtifact;

    public DeployerBundleArtifact(@NonNull BundleContext bundleContext, @NonNull BundleInstallArtifact installArtifact, @NonNull Region region) {
        super(bundleContext, installArtifact, region);
        this.installArtifact = installArtifact;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteEntry(String targetPath) {
        this.installArtifact.deleteEntry(targetPath);
    }

    /**
     * {@inheritDoc}
     */
    public void updateEntry(String inputPath, String targetPath) {
        this.installArtifact.updateEntry(URI.create(inputPath), targetPath);
    }

    /**
     * {@inheritDoc}
     */
    public final Map<String, String> getProperties() {
        Map<String, String> parentProperties = super.getProperties();
        Map<String, String> properties = new HashMap<String, String>();
        properties.putAll(parentProperties);
        properties.put("BundleId", String.valueOf(this.installArtifact.getBundle().getBundleId()));
        properties.put("Location", this.installArtifact.getBundle().getLocation());
        return Collections.unmodifiableMap(properties);
    }
    
}
