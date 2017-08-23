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

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.SpringContextAccessor;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final RegionDigraph regionDigraph;

    private final SpringContextAccessor springContextAccessor;

    public ModelBundleListener(@NonNull BundleContext bundleContext, @NonNull RuntimeArtifactRepository artifactRepository, @NonNull PackageAdminUtil packageAdminUtil, @NonNull RegionDigraph regionDigraph, @NonNull SpringContextAccessor springContextAccessor) {
        this.bundleContext = bundleContext;
        this.artifactRepository = artifactRepository;
        this.packageAdminUtil = packageAdminUtil;
        this.regionDigraph = regionDigraph;
        this.springContextAccessor = springContextAccessor;
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
        Region region = this.regionDigraph.getRegion(bundle);
        logger.info("Processing installed event for bundle '{}:{}' in region '{}'", new Object[] {bundle.getSymbolicName(), bundle.getVersion().toString(), region.getName()});
        this.artifactRepository.add(new NativeBundleArtifact(this.bundleContext, this.packageAdminUtil, bundle, region, this.springContextAccessor));
    }

    private void processUninstalled(BundleEvent event) {
        Bundle bundle = event.getBundle();
        for (Artifact artifact : this.artifactRepository.getArtifacts()) {
            if (artifact.getType().equals(NativeBundleArtifact.TYPE) && 
                artifact.getName().equals(bundle.getSymbolicName()) && 
                artifact.getVersion().equals(bundle.getVersion()) ){
                if(artifact instanceof NativeBundleArtifact){
                    NativeBundleArtifact bundleArtifact = (NativeBundleArtifact) artifact;
                    if(ArtifactState.UNINSTALLED == bundleArtifact.getState()){
                        this.artifactRepository.remove(bundleArtifact);
                        logger.info("Processing uninstalled event for bundle '{}:{}' from region '{}'", new Object[] {bundleArtifact.getName(), bundleArtifact.getVersion().toString(), bundleArtifact.getRegion().getName()});
                    }
                }
            }
        }
    }

}
