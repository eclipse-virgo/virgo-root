/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.util.Map;

import org.eclipse.virgo.nano.deployer.api.config.ConfigurationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.kernel.install.artifact.ConfigInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphFactory;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.GraphNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * {@link ConfigInstallArtifactGraphFactory} is an {@link InstallArtifactGraphFactory} for configuration properties file
 * {@link InstallArtifact InstallArtifacts}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class ConfigInstallArtifactGraphFactory extends AbstractArtifactGraphFactory {

    private static final String PROPERTIES_TYPE = ArtifactIdentityDeterminer.CONFIGURATION_TYPE;

    private final BundleContext bundleContext;

    private final ConfigLifecycleEngine lifecycleEngine;

    private final EventLogger eventLogger;
    
    private final Object monitor = new Object();

    private ConfigurationDeployer configurationDeployer;

    ConfigInstallArtifactGraphFactory(BundleContext bundleContext, EventLogger eventLogger, @NonNull DirectedAcyclicGraph<InstallArtifact> dag) {
    	super(dag);
        this.bundleContext = bundleContext;
        this.lifecycleEngine = new ConfigLifecycleEngine(bundleContext);
        this.eventLogger = eventLogger;
    }

    /**
     * {@inheritDoc}
     */
    public GraphNode<InstallArtifact> constructInstallArtifactGraph(ArtifactIdentity artifactIdentity, ArtifactStorage artifactStorage, Map<String, String> deploymentProperties, String repositoryName) throws DeploymentException {
        if (PROPERTIES_TYPE.equalsIgnoreCase(artifactIdentity.getType())) {
            ConfigurationDeployer configDeployer = obtainConfigurationDeployer();
            if(configDeployer == null){
            	throw new DeploymentException(String.format("Unable to locate a '%s' service to deploy '%s'", ConfigurationDeployer.class.getName(), artifactIdentity.getName()));
            }
            ArtifactStateMonitor artifactStateMonitor = new StandardArtifactStateMonitor(this.bundleContext);
            ConfigInstallArtifact configInstallArtifact = new StandardConfigInstallArtifact(artifactIdentity, artifactStorage, this.lifecycleEngine, this.lifecycleEngine, this.lifecycleEngine, artifactStateMonitor, repositoryName, eventLogger, configDeployer);
            return constructAssociatedGraphNode(configInstallArtifact);
        } else {
            return null;
        }
    }

    private ConfigurationDeployer obtainConfigurationDeployer() throws DeploymentException {
        synchronized (this.monitor) {
            if (this.configurationDeployer == null) {
                ServiceReference<ConfigurationDeployer> serviceReference = this.bundleContext.getServiceReference(ConfigurationDeployer.class);
                this.configurationDeployer = this.bundleContext.getService(serviceReference);
            }
            return this.configurationDeployer;
        }
    }

}
