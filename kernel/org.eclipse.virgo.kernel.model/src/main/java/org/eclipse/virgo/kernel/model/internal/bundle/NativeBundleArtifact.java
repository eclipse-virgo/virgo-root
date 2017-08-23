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

package org.eclipse.virgo.kernel.model.internal.bundle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.internal.AbstractArtifact;
import org.eclipse.virgo.kernel.model.internal.SpringContextAccessor;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Artifact} that delegates to an OSGi native bundle, this is in comparison 
 * to a {@link DeployerBundleArtifact} which delegates to a Kernel {@link InstallArtifact}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
final class NativeBundleArtifact extends AbstractArtifact {

    static final String TYPE = "bundle";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PackageAdminUtil packageAdminUtil;

    private final Bundle bundle;

    private final SpringContextAccessor springContextAccessor;
    
    public NativeBundleArtifact(@NonNull BundleContext bundleContext, @NonNull PackageAdminUtil packageAdminUtil, @NonNull Bundle bundle, @NonNull Region region, @NonNull SpringContextAccessor springContextAccessor) {
        super(bundleContext, TYPE, bundle.getSymbolicName(), bundle.getVersion(), region);
        this.packageAdminUtil = packageAdminUtil;
        this.bundle = bundle;
        this.springContextAccessor = springContextAccessor;
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactState getState() {
        return mapBundleState(this.bundle.getState());
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("Bundle Id", String.valueOf(this.bundle.getBundleId()));
        properties.put("Spring", String.valueOf(this.springContextAccessor.isSpringPowered(bundle)));
        return Collections.unmodifiableMap(properties);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean refresh() {
        try {
            this.bundle.update();
            this.packageAdminUtil.synchronouslyRefreshPackages(new Bundle[] { this.bundle });
            return true;
        } catch (BundleException e) {
            logger.error("Unable to update bundle '{}:{}'", this.bundle.getSymbolicName(), this.bundle.getVersion());
            throw new RuntimeException(String.format("Unable to update bundle '%s:%s'", this.bundle.getSymbolicName(), this.bundle.getVersion()), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
        try {
            this.bundle.start();
        } catch (BundleException e) {
            logger.error("Unable to start bundle '{}:{}'", this.bundle.getSymbolicName(), this.bundle.getVersion());
            throw new RuntimeException(String.format("Unable to start bundle '%s:%s'", this.bundle.getSymbolicName(), this.bundle.getVersion()), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        try {
            this.bundle.stop();
        } catch (BundleException e) {
            logger.error("Unable to stop bundle '{}:{}'", this.bundle.getSymbolicName(), this.bundle.getVersion());
            throw new RuntimeException(String.format("Unable to stop bundle '%s:%s'", this.bundle.getSymbolicName(), this.bundle.getVersion()), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void uninstall() {
        try {
            this.bundle.uninstall();
        } catch (BundleException e) {
            logger.error("Unable to uninstall bundle '{}:{}'", this.bundle.getSymbolicName(), this.bundle.getVersion());
            throw new RuntimeException(String.format("Unable to uninstall bundle '%s:%s'", this.bundle.getSymbolicName(), this.bundle.getVersion()), e);
        }
    }

    static ArtifactState mapBundleState(int state) {
        if (Bundle.UNINSTALLED == state) {
            return ArtifactState.UNINSTALLED;
        } else if (Bundle.INSTALLED == state) {
            return ArtifactState.INSTALLED;
        } else if (Bundle.RESOLVED == state) {
            return ArtifactState.RESOLVED;
        } else if (Bundle.STARTING == state) {
            return ArtifactState.STARTING;
        } else if (Bundle.STOPPING == state) {
            return ArtifactState.STOPPING;
        } else if (Bundle.ACTIVE == state) {
            return ArtifactState.ACTIVE;
        } else {
            throw new IllegalArgumentException(String.format("Unknown bundle state '%d'", state));
        }
    }
    
}
