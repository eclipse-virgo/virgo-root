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

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;

/**
 * Implementation of {@link BundleListener} that notices {@link BundleEvent#INSTALLED} and
 * {@link BundleEvent#UNINSTALLED} events to add and remove respectively {@link Artifact}s from the
 * {@link RuntimeArtifactRepository}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
final class ModelBundleListener implements SynchronousBundleListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BundleContext bundleContext;

    private final RuntimeArtifactRepository artifactRepository;

    private final PackageAdminUtil packageAdminUtil;

    public ModelBundleListener(@NonNull BundleContext bundleContext, @NonNull RuntimeArtifactRepository artifactRepository,
        @NonNull PackageAdminUtil packageAdminUtil) {
        this.bundleContext = bundleContext;
        this.artifactRepository = artifactRepository;
        this.packageAdminUtil = packageAdminUtil;
    }

    /**
     * {@inheritDoc}
     */
    public void bundleChanged(BundleEvent event) {
        if (BundleEvent.INSTALLED == event.getType()) {
            processInstalled(event);
        } else if (BundleEvent.UNINSTALLED == event.getType()) {
            processUninstalled(event);
        }
    }

    private void processInstalled(BundleEvent event) {
        Bundle bundle = event.getBundle();
        logger.info("Processing installed event for '{}:{}'", bundle.getSymbolicName(), bundle.getVersion().toString());
        this.artifactRepository.add(new BundleArtifact(bundleContext, packageAdminUtil, bundle));
    }

    private void processUninstalled(BundleEvent event) {
        Bundle bundle = event.getBundle();
        logger.info("Processing uninstalled event for '{}:{}'", bundle.getSymbolicName(), bundle.getVersion().toString());
        this.artifactRepository.remove(BundleArtifact.TYPE, bundle.getSymbolicName(), bundle.getVersion());
    }

}
