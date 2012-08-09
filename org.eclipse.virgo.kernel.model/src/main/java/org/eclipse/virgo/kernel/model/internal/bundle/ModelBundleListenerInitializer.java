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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.SpringContextAccessor;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An initializer responsible for registering a {@link ModelBundleListener} and enumerating any existing {@link Bundle}
 * objects from the OSGi Framework.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe
 * 
 * @see ModelBundleListener
 */
public final class ModelBundleListenerInitializer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RuntimeArtifactRepository artifactRepository;

    private final PackageAdminUtil packageAdminUtil;

    private final BundleContext kernelBundleContext;

    private final BundleListener bundleListener;

    private final RegionDigraph regionDigraph;

    private final SpringContextAccessor springContextAccessor;

    public ModelBundleListenerInitializer(@NonNull RuntimeArtifactRepository artifactRepository, @NonNull PackageAdminUtil packageAdminUtil, @NonNull BundleContext kernelBundleContext, @NonNull RegionDigraph regionDigraph, @NonNull SpringContextAccessor springContextAccessor) {
        this.artifactRepository = artifactRepository;
        this.packageAdminUtil = packageAdminUtil;
        this.kernelBundleContext = kernelBundleContext;
        this.bundleListener = new ModelBundleListener(kernelBundleContext, artifactRepository, packageAdminUtil, regionDigraph, springContextAccessor);
        this.regionDigraph = regionDigraph;
        this.springContextAccessor = springContextAccessor;
    }

    /**
     * Registers a {@link BundleListener} with the OSGi framework. Enumerates any existing {@link Bundle}s that exist
     * in the user region.
     */
    @PostConstruct
    public void initialize() {
        BundleContext systemBundleContext = getSystemBundleContext();
        // Register the listener with the system bundle context to see all bundles in all regions.
       systemBundleContext.addBundleListener(this.bundleListener);
        // Find bundles that the listener has almost certainly missed.
        for (Bundle bundle : systemBundleContext.getBundles()) {
            try {
                this.artifactRepository.add(new NativeBundleArtifact(this.kernelBundleContext, this.packageAdminUtil, bundle, this.regionDigraph.getRegion(bundle), this.springContextAccessor));
            } catch (Exception e) {
                this.logger.error(String.format("Exception adding bundle '%s:%s' to the repository", bundle.getSymbolicName(),
                    bundle.getVersion().toString()), e);
            }
        }
    }

    private BundleContext getSystemBundleContext() {
        BundleContext systemBundleContext = this.kernelBundleContext.getBundle(0L).getBundleContext();
        return systemBundleContext;
    }

    /**
     * Unregisters the listener from the OSGi framework
     */
    @PreDestroy
    public void destroy() {
        getSystemBundleContext().removeBundleListener(this.bundleListener);
    }
}
