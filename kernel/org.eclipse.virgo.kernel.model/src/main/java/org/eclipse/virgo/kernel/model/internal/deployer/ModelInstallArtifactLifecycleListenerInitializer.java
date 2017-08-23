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

import javax.annotation.PostConstruct;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.SpringContextAccessor;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An initializer responsible for registering a {@link ModelInstallArtifactLifecycleListener} and enumerating any
 * existing {@link InstallArtifact} objects from the Kernel deployer
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 * @see ModelInstallArtifactLifecycleListener
 */
public final class ModelInstallArtifactLifecycleListenerInitializer {

    private static final String USER_REGION_NAME = "org.eclipse.virgo.region.user";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RuntimeArtifactRepository artifactRepository;

    private final BundleContext bundleContext;

    private final RuntimeArtifactModel runtimeArtifactModel;

    private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();

    private final RegionDigraph regionDigraph;

    private final Region globalRegion;
    
    private final SpringContextAccessor springContextAccessor;

    public ModelInstallArtifactLifecycleListenerInitializer(@NonNull RuntimeArtifactRepository artifactRepository, @NonNull BundleContext bundleContext, @NonNull RuntimeArtifactModel runtimeArtifactModel, @NonNull RegionDigraph regionDigraph, @NonNull Region globalRegion, @NonNull SpringContextAccessor springContextAccessor) {
        this.artifactRepository = artifactRepository;
        this.bundleContext = bundleContext;
        this.runtimeArtifactModel = runtimeArtifactModel;
        this.regionDigraph = regionDigraph;
        this.globalRegion = globalRegion;
        this.springContextAccessor = springContextAccessor;
    }

    /**
     * Registers a {@link ModelInstallArtifactLifecycleListener} with the service registry. Enumerates any existing
     * {@link InstallArtifact} objects that exist from the Kernel deployer
     */
    @PostConstruct
    public void initialize() {
        ModelInstallArtifactLifecycleListener listener = new ModelInstallArtifactLifecycleListener(this.bundleContext, this.artifactRepository, this.regionDigraph, this.globalRegion, this.springContextAccessor);
        this.registrationTracker.track(this.bundleContext.registerService(InstallArtifactLifecycleListener.class.getCanonicalName(), listener, null));
        for (DeploymentIdentity deploymentIdentity : this.runtimeArtifactModel.getDeploymentIdentities()) {
            InstallArtifact installArtifact = this.runtimeArtifactModel.get(deploymentIdentity);
            try {
                if (installArtifact instanceof PlanInstallArtifact) {
                    this.artifactRepository.add(new DeployerCompositeArtifact(this.bundleContext, (PlanInstallArtifact) installArtifact, this.globalRegion));
                } else if (installArtifact instanceof BundleInstallArtifact) {
                    this.artifactRepository.remove(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(), getRegion(USER_REGION_NAME));
                    BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
                    this.artifactRepository.add(new DeployerBundleArtifact(this.bundleContext, bundleInstallArtifact, getRegion(USER_REGION_NAME), this.springContextAccessor));
                } else {
                    this.artifactRepository.remove(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(), null);
                    this.artifactRepository.add(new DeployerArtifact(this.bundleContext, installArtifact, this.globalRegion));
                }
            } catch (Exception e) {
                logger.error(String.format("Exception adding deployer artifact '%s:%s' to the repository", installArtifact.getName(),
                    installArtifact.getVersion().toString()), e);
            }
        }
    }

    /**
     * Unregisters the listener from the service registry
     */
    public void destroy() {
        this.registrationTracker.unregisterAll();
    }
    
    private Region getRegion(String name){
        return this.regionDigraph.getRegion(name);
    }
    
}
