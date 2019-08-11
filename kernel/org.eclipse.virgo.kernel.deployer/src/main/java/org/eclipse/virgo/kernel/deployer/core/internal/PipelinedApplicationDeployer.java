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

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.nano.core.KernelException;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeployUriNormaliser;
import org.eclipse.virgo.nano.deployer.api.core.DeployerConfiguration;
import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.kernel.deployer.core.internal.event.DeploymentListener;
import org.eclipse.virgo.kernel.deployer.model.DuplicateDeploymentIdentityException;
import org.eclipse.virgo.kernel.deployer.model.DuplicateFileNameException;
import org.eclipse.virgo.kernel.deployer.model.DuplicateLocationException;
import org.eclipse.virgo.kernel.deployer.model.GCRoots;
import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphInclosure;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.internal.AbstractInstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironmentFactory;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.WatchableRepository;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.io.PathReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * {@link PipelinedApplicationDeployer} is an implementation of {@link ApplicationDeployer} which creates a
 * {@link GraphNode} of {@link InstallArtifact InstallArtifacts} and processes the graph by passing it through a
 * {@link Pipeline} while operating on an {@link InstallEnvironment}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class PipelinedApplicationDeployer implements ApplicationDeployer, ApplicationRecoverer {
    
    private static final String BUNDLE_TYPE = "bundle";
    
    private final EventLogger eventLogger;
    
    private final Object monitor = new Object();
    
    private final InstallEnvironmentFactory installEnvironmentFactory;
    
    private final InstallArtifactGraphInclosure installArtifactGraphInclosure;
    
    private final ArtifactIdentityDeterminer artifactIdentityDeterminer;
    
    private final RuntimeArtifactModel ram;
    
    private final DeploymentListener deploymentListener;
    
    private final Map<DeploymentIdentity, DeploymentOptions> deploymentOptionsMap = new HashMap<DeploymentIdentity, DeploymentOptions>();
    
    private final Pipeline pipeline;
    
    private final DeployUriNormaliser deployUriNormaliser;
    
    private final int deployerConfiguredTimeoutInSeconds;
    
    private final BundleContext bundleContext;
    
    public PipelinedApplicationDeployer(@NonNull Pipeline pipeline, @NonNull InstallArtifactGraphInclosure installArtifactGraphInclosure,
                                        @NonNull ArtifactIdentityDeterminer artifactIdentityDeterminer, @NonNull InstallEnvironmentFactory installEnvironmentFactory,
                                        @NonNull RuntimeArtifactModel ram, @NonNull DeploymentListener deploymentListener, @NonNull EventLogger eventLogger,
                                        @NonNull DeployUriNormaliser normaliser, @NonNull DeployerConfiguration deployerConfiguration, @NonNull BundleContext bundleContext) {
        this.eventLogger = eventLogger;
        this.installArtifactGraphInclosure = installArtifactGraphInclosure;
        this.artifactIdentityDeterminer = artifactIdentityDeterminer;
        this.installEnvironmentFactory = installEnvironmentFactory;
        this.ram = ram;
        this.deploymentListener = deploymentListener;
        this.deployUriNormaliser = normaliser;
        this.bundleContext = bundleContext;
        this.pipeline = pipeline;
        this.deployerConfiguredTimeoutInSeconds = deployerConfiguration.getDeploymentTimeoutSeconds();
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity deploy(URI location) throws DeploymentException {
        synchronized (this.monitor) {
            return deploy(location, new DeploymentOptions());
        }
    }
    
    private URI normaliseDeploymentUri(URI uri) throws DeploymentException {
        URI normalisedLocation = this.deployUriNormaliser.normalise(uri);
        
        if (normalisedLocation == null) {
            this.eventLogger.log(DeployerLogEvents.UNSUPPORTED_URI_SCHEME, uri, uri.getScheme());
            throw new DeploymentException("PipelinedApplicationDeployer.deploy does not support '" + uri.getScheme() + "' scheme URIs");
        }
        
        return normalisedLocation;
    }
    
    public DeploymentIdentity install(URI location) throws DeploymentException {
        return install(location, new DeploymentOptions());
    }
    
    public DeploymentIdentity install(URI uri, DeploymentOptions deploymentOptions) throws DeploymentException {
        URI normalisedUri = normaliseDeploymentUri(uri);
        
        DeploymentIdentity deploymentIdentity = doInstall(normalisedUri, deploymentOptions);
        this.deploymentListener.deployed(normalisedUri, deploymentOptions);
        
        return deploymentIdentity;
    }
    
    private DeploymentIdentity doInstall(URI normalisedUri, DeploymentOptions deploymentOptions) throws DeploymentException {
        synchronized (this.monitor) {
            InstallArtifact existingArtifact = this.ram.get(normalisedUri);
            
            if (existingArtifact != null) {
                DeploymentIdentity refreshedIdentity = refreshExistingArtifact(normalisedUri, existingArtifact);
                if (refreshedIdentity != null) {
                    return refreshedIdentity;
                }
            }
            
            GraphNode<InstallArtifact> installNode;
            boolean shared = false;
            try {
                ArtifactIdentity artifactIdentity = determineIdentity(normalisedUri);
                installNode = findSharedNode(artifactIdentity);
                if (installNode == null) {
                    installNode = this.installArtifactGraphInclosure.constructGraphNode(artifactIdentity, new File(normalisedUri), null, null);
                } else {
                    shared = true;
                }
            } catch (Exception e) {
                throw new DeploymentException(e.getMessage() + ": uri='" + normalisedUri + "'", e);
            }
            
            DeploymentIdentity deploymentIdentity;
            
            try {
                deploymentIdentity = addGraphToModel(normalisedUri, installNode);
            } catch (KernelException ke) {
                if (!shared) {
                    destroyInstallGraph(installNode);
                }
                throw new DeploymentException(ke.getMessage(), ke);
            }
            
            if (!shared) {
                this.deploymentOptionsMap.put(deploymentIdentity, deploymentOptions);
                try {
                    driveInstallPipeline(normalisedUri, installNode);
                } catch (DeploymentException de) {
                    removeFromModel(deploymentIdentity);
                    destroyInstallGraph(installNode);
                    throw de;
                } catch (RuntimeException re) {
                    removeFromModel(deploymentIdentity);
                    destroyInstallGraph(installNode);
                    throw re;
                }
            }
            
            return deploymentIdentity;
        }
    }
    
    private ArtifactIdentity determineIdentity(URI artifactUri) throws DeploymentException {
        try {
            File artifact = new File(artifactUri);
            if (!artifact.exists()) {
                throw new DeploymentException(artifact + " does not exist");
            }
            
            return determineIdentity(artifact, null);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage() + ": uri='" + artifactUri + "'", e);
        }
    }
    
    private GraphNode<InstallArtifact> findSharedNode(ArtifactIdentity artifactIdentity) {
        GCRoots gcRoots = (GCRoots) this.ram;
        return ExistingNodeLocator.findSharedNode(gcRoots, artifactIdentity);
    }
    
    private void destroyInstallGraph(GraphNode<InstallArtifact> installGraph) throws DeploymentException {
        installGraph.getValue().uninstall();
    }
    
    private void removeFromModel(DeploymentIdentity deploymentIdentity) throws DeploymentException {
        this.ram.delete(deploymentIdentity);
    }
    
    private DeploymentIdentity refreshExistingArtifact(URI normalisedLocation, InstallArtifact existingArtifact) throws DeploymentException {
        String oldType = existingArtifact.getType();
        String oldName = existingArtifact.getName();
        Version oldVersion = existingArtifact.getVersion();
        
        DeploymentIdentity deploymentIdentity = refreshArtifact(normalisedLocation, existingArtifact);
        if (deploymentIdentity != null) {
            return deploymentIdentity;
        }
        
        DeploymentIdentity oldDeploymentIdentity = new StandardDeploymentIdentity(oldType, oldName, oldVersion.toString());
        undeployInternal(oldDeploymentIdentity, true, false);
        
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity deploy(URI location, DeploymentOptions deploymentOptions) throws DeploymentException {
        URI normalisedLocation = normaliseDeploymentUri(location);
        
        InstallArtifact installedArtifact;
        DeploymentIdentity deploymentIdentity;
        
        synchronized (this.monitor) {
            deploymentIdentity = install(location, deploymentOptions);
            installedArtifact = this.ram.get(normalisedLocation);
        }
        
        try {
            start(installedArtifact, deploymentOptions.getSynchronous());
        } catch (DeploymentException de) {
            synchronized (this.monitor) {
                stopArtifact(installedArtifact);
                uninstallArtifact(installedArtifact);
            }
            throw de;
        }
        
        this.deploymentListener.deployed(normalisedLocation, deploymentOptions);
        
        return deploymentIdentity;
    }
    
    private DeploymentIdentity refreshArtifact(URI location, InstallArtifact installArtifact) throws DeploymentException {
        DeploymentIdentity deploymentIdentity = null;
        if (installArtifact.refresh()) {
            this.deploymentListener.refreshed(location);
            
            deploymentIdentity = new StandardDeploymentIdentity(installArtifact.getType(), installArtifact.getName(),
                                                                installArtifact.getVersion().toString());
        }
        return deploymentIdentity;
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity deploy(String type, String name, Version version) throws DeploymentException {
        throw new UnsupportedOperationException(
                                                "PipelinedApplicationDeployer ApplicationDeployer does not support deployment by type, name, and version");
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity deploy(String type, String name, Version version, DeploymentOptions options) throws DeploymentException {
        throw new UnsupportedOperationException(
                                                "PipelinedApplicationDeployer ApplicationDeployer does not support deployment by type, name, and version");
    }
    
    private DeploymentIdentity addGraphToModel(URI location, GraphNode<InstallArtifact> installGraph) throws DuplicateFileNameException,
    DuplicateLocationException, DuplicateDeploymentIdentityException, DeploymentException {
        InstallArtifact installArtifact = installGraph.getValue();
        ((AbstractInstallArtifact) installArtifact).setTopLevelDeployed();
        return this.ram.add(location, installArtifact);
    }
    
    /**
     * {@inheritDoc}
     */
    public void recoverDeployment(URI uri, DeploymentOptions options) throws DeploymentException {
        
        GraphNode<InstallArtifact> installNode = null;
        boolean shared = false;
        File artifact = new File(uri);
        if (options.getRecoverable() && (!options.getDeployerOwned() || artifact.exists())) {
            ArtifactIdentity artifactIdentity = determineIdentity(artifact, null);
            installNode = findSharedNode(artifactIdentity);
            if (installNode == null) {
                installNode = this.installArtifactGraphInclosure.recoverInstallGraph(artifactIdentity, artifact);
            } else {
                shared = true;
            }
        }
        
        if (installNode == null) {
            // Remove the URI from the recovery log.
            this.deploymentListener.undeployed(uri);
        } else {
            if (!shared) {
                driveInstallPipeline(uri, installNode);
                
                start(installNode.getValue(), options.getSynchronous());
            }
            
            try {
                addGraphToModel(uri, installNode);
            } catch (KernelException e) {
                throw new DeploymentException(e.getMessage(), e);
            }
        }
    }
    
    private ArtifactIdentity determineIdentity(File file, String scopeName) throws DeploymentException {
        ArtifactIdentity artifactIdentity = this.artifactIdentityDeterminer.determineIdentity(file, scopeName);
        
        if (artifactIdentity == null) {
            this.eventLogger.log(DeployerLogEvents.INDETERMINATE_ARTIFACT_TYPE, file);
            throw new DeploymentException("Cannot determine the artifact identity of the file '" + file + "'");
        }
        
        return artifactIdentity;
    }
    
    private void driveInstallPipeline(URI uri, GraphNode<InstallArtifact> installGraph) throws DeploymentException {
        
        refreshWatchedRepositories();
        InstallEnvironment installEnvironment = this.installEnvironmentFactory.createInstallEnvironment(installGraph.getValue());
        
        try {
            this.pipeline.process(installGraph, installEnvironment);
        } catch (UnableToSatisfyBundleDependenciesException utsbde) {
            logDependencySatisfactionException(uri, utsbde);
            throw new DeploymentException("Dependency satisfaction failed", utsbde);
        } finally {
            installEnvironment.destroy();
        }
    }
    
    private void logDependencySatisfactionException(URI uri, UnableToSatisfyDependenciesException ex) {
        this.eventLogger.log(DeployerLogEvents.UNABLE_TO_SATISFY_CONSTRAINTS, ex, uri, ex.getSymbolicName(), ex.getVersion(),
                             ex.getFailureDescription());
    }
    
    private void start(InstallArtifact installArtifact, boolean synchronous) throws DeploymentException {
        BlockingAbortableSignal blockingSignal = new BlockingAbortableSignal(synchronous);
        installArtifact.start(blockingSignal);
        if (synchronous && this.deployerConfiguredTimeoutInSeconds > 0) {
            boolean complete = blockingSignal.awaitCompletion(this.deployerConfiguredTimeoutInSeconds);
            if (blockingSignal.isAborted()) {
                this.eventLogger.log(DeployerLogEvents.START_ABORTED, installArtifact.getType(), installArtifact.getName(),
                                     installArtifact.getVersion(), this.deployerConfiguredTimeoutInSeconds);
            } else if (!complete) {
                this.eventLogger.log(DeployerLogEvents.START_TIMED_OUT, installArtifact.getType(), installArtifact.getName(),
                                     installArtifact.getVersion(), this.deployerConfiguredTimeoutInSeconds);
            }
        } else {
            // Completion messages will have been issued if complete, so ignore return value.
            blockingSignal.checkComplete();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity[] getDeploymentIdentities() {
        synchronized (this.monitor) {
            return this.ram.getDeploymentIdentities();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity getDeploymentIdentity(URI location) {
        synchronized (this.monitor) {
            InstallArtifact installArtifact = this.ram.get(location);
            if (installArtifact != null) {
                return getDeploymentIdentity(installArtifact);
            }
        }
        return null;
    }
    
    private DeploymentIdentity getDeploymentIdentity(InstallArtifact installArtifact) {
        return new StandardDeploymentIdentity(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion().toString());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDeployed(URI location) {
        URI normalisedLocation;
        try {
            normalisedLocation = this.deployUriNormaliser.normalise(location);
        } catch (DeploymentException e) {
            return false;
        }
        
        if (normalisedLocation == null) {
            this.eventLogger.log(DeployerLogEvents.UNSUPPORTED_URI_SCHEME, location.toString(), location.getScheme());
            return false;
        }
        synchronized (this.monitor) {
            return this.ram.get(normalisedLocation) != null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public DeploymentIdentity refresh(URI location, String symbolicName) throws DeploymentException {
        URI normalisedLocation = this.deployUriNormaliser.normalise(location);
        
        if (normalisedLocation == null) {
            this.eventLogger.log(DeployerLogEvents.UNSUPPORTED_URI_SCHEME, location.toString(), location.getScheme());
            throw new DeploymentException("PipelinedApplicationDeployer.refresh does not support '" + location.getScheme() + "' scheme URIs");
        }
        
        DeploymentIdentity deploymentIdentity;
        synchronized (this.monitor) {
            InstallArtifact installArtifact = this.ram.get(normalisedLocation);
            if (installArtifact == null) {
                this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_URI_NOT_FOUND, location.toString());
                throw new DeploymentException("Refresh not possible as no application is deployed from URI " + location);
            } else {
                DeploymentIdentity originalDeploymentIdentity = getDeploymentIdentity(installArtifact);
                deploymentIdentity = originalDeploymentIdentity;
                try {
                    // Attempt to refresh the artifact and escalate to redeploy if this fails.
                    if (refreshInternal(symbolicName, installArtifact)) {
                        this.deploymentListener.refreshed(normalisedLocation);
                    } else {
                        DeploymentOptions deploymentOptions = this.deploymentOptionsMap.get(deploymentIdentity);
                        if (deploymentOptions == null) {
                            deploymentOptions = DeploymentOptions.DEFAULT_DEPLOYMENT_OPTIONS;
                        }
                        deploymentIdentity = redeploy(originalDeploymentIdentity, normalisedLocation, deploymentOptions);
                    }
                    this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_COMPLETED, symbolicName, originalDeploymentIdentity.getType(),
                                         originalDeploymentIdentity.getSymbolicName(), originalDeploymentIdentity.getVersion());
                } catch (RuntimeException e) {
                    this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_FAILED, e, symbolicName, originalDeploymentIdentity.getType(),
                                         originalDeploymentIdentity.getSymbolicName(), originalDeploymentIdentity.getVersion());
                    throw e;
                } catch (Exception e) {
                    this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_FAILED, e, symbolicName, originalDeploymentIdentity.getType(),
                                         originalDeploymentIdentity.getSymbolicName(), originalDeploymentIdentity.getVersion());
                    throw new DeploymentException("refresh failed", e);
                }
            }
        }
        return deploymentIdentity;
    }
    
    private boolean refreshInternal(String symbolicName, InstallArtifact installArtifact) throws DeploymentException {
        if (installArtifact instanceof PlanInstallArtifact) {
            return ((PlanInstallArtifact) installArtifact).refresh(symbolicName);
        } else {
            return installArtifact.refresh();
        }
    }
    
    private DeploymentIdentity redeploy(DeploymentIdentity toUndeploy, URI toDeploy, DeploymentOptions deploymentOptions) throws DeploymentException {
        synchronized (this.monitor) {
            undeployInternal(toUndeploy, true, false);
        }
        return deploy(toDeploy, deploymentOptions);
    }
    
    /**
     * {@inheritDoc}
     */
    public void refreshBundle(String bundleSymbolicName, String bundleVersion) throws DeploymentException {
        DeploymentIdentity deploymentIdentity = new StandardDeploymentIdentity(BUNDLE_TYPE, bundleSymbolicName, bundleVersion);
        InstallArtifact bundleInstallArtifact;
        synchronized (this.monitor) {
            bundleInstallArtifact = this.ram.get(deploymentIdentity);
        }
        if (bundleInstallArtifact == null) {
            this.eventLogger.log(DeployerLogEvents.REFRESH_ARTEFACT_NOT_FOUND, BUNDLE_TYPE, bundleSymbolicName, bundleVersion);
            throw new DeploymentException("Refresh not possible as no " + BUNDLE_TYPE + " with name " + bundleSymbolicName + " and version "
                                          + bundleVersion + " is deployed");
        }
        bundleInstallArtifact.refresh();
    }
    
    /**
     * {@inheritDoc}
     */
    public void undeploy(String symbolicName, String version) throws DeploymentException {
        // This method is deprecated and should be deleted when it is no longer used. Meanwhile, just try undeploying
        // the possible types...
        DeploymentException de = null;
        try {
            undeploy(BUNDLE_TYPE, symbolicName, version);
            return;
        } catch (DeploymentException e) {
            de = e;
        }
        
        try {
            undeploy("par", symbolicName, version);
            return;
        } catch (DeploymentException e) {
            de = e;
        }
        
        try {
            undeploy("plan", symbolicName, version);
            return;
        } catch (DeploymentException e) {
            de = e;
        }
        
        try {
            undeploy("properties", symbolicName, version);
            return;
        } catch (DeploymentException e) {
            de = e;
        }
        
        throw de;
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void undeploy(String type, String symbolicName, String version) throws DeploymentException {
        DeploymentIdentity deploymentIdentity = new StandardDeploymentIdentity(type, symbolicName, version);
        synchronized (this.monitor) {
            undeployInternal(deploymentIdentity, false, false);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void undeploy(DeploymentIdentity deploymentIdentity) throws DeploymentException {
        synchronized (this.monitor) {
            undeployInternal(deploymentIdentity, false, false);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void undeploy(DeploymentIdentity deploymentIdentity, boolean deleted) throws DeploymentException {
        synchronized (this.monitor) {
            undeployInternal(deploymentIdentity, false, deleted);
        }
    }
    
    /**
     * All the undeploy work goes on in here -- it is assumed that any required monitors are already held by the caller.
     * <p>
     * The deleted parameter indicates whether the undeployment is a consequence of the artifact having been deleted.
     * This affects the processing of "deployer owned" artifacts which undeploy would normally delete automatically. If
     * the undeploy is a consequence of the artifact having been deleted, then undeploy must not delete the artifact
     * automatically since this may actually delete a "new" artifact which has arrived shortly after the "old" artifact
     * was deleted.
     * 
     * @param deploymentIdentity identity of artifact to undeploy
     * @param redeploying flag to indicate if we are performing a re-deploy
     * @param deleted <code>true</code> if and only if undeploy is being driven as a consequence of the artifact having
     *        been deleted
     * @throws DeploymentException
     */
    private void undeployInternal(DeploymentIdentity deploymentIdentity, boolean redeploying, boolean deleted) throws DeploymentException {
        DeploymentOptions options = this.deploymentOptionsMap.remove(deploymentIdentity);
        URI location = doUndeploy(deploymentIdentity);
        if (location != null && !redeploying) {
            deleteArtifactIfNecessary(location, options, deleted);
        }
    }
    
    private void deleteArtifactIfNecessary(URI location, DeploymentOptions options, boolean deleted) {
        if (options != null && options.getDeployerOwned() && !deleted) {
            new PathReference(location).delete(true);
        }
    }
    
    private URI doUndeploy(DeploymentIdentity deploymentIdentity) throws DeploymentException {
        synchronized (this.monitor) {
            InstallArtifact installArtifact = this.ram.get(deploymentIdentity);
            if (installArtifact == null) {
                String type = deploymentIdentity.getType();
                String symbolicName = deploymentIdentity.getSymbolicName();
                String version = deploymentIdentity.getVersion();
                this.eventLogger.log(DeployerLogEvents.UNDEPLOY_ARTEFACT_NOT_FOUND, type, symbolicName, version);
                throw new DeploymentException("Undeploy not possible as no " + type + " with name " + symbolicName + " and version " + version
                                              + " is deployed");
            } else {
                URI location = this.ram.getLocation(deploymentIdentity);
                
                this.ram.delete(deploymentIdentity);
                
                stopArtifact(installArtifact);
                uninstallArtifact(installArtifact);
                return location;
            }
        }
    }
    
    private void stopArtifact(InstallArtifact installArtifact) throws DeploymentException {
        
        installArtifact.stop();
        
    }
    
    private void uninstallArtifact(InstallArtifact installArtifact) throws DeploymentException {
        installArtifact.uninstall();
    }
    
    private void refreshWatchedRepositories() {
        try {
            Collection<ServiceReference<WatchableRepository>> references = this.bundleContext.getServiceReferences(WatchableRepository.class, null);
            for (ServiceReference<WatchableRepository> reference : references) {
                WatchableRepository watchableRepository = this.bundleContext.getService(reference);
                try {
                    watchableRepository.forceCheck();
                } catch (Exception e) {
                    String name;
                    if (watchableRepository instanceof Repository) {
                        name = ((Repository) watchableRepository).getName();
                    } else {
                        name = "unknown repository type";
                    }
                    this.eventLogger.log(DeployerLogEvents.WATCHED_REPOSITORY_REFRESH_FAILED, name);
                }
                this.bundleContext.ungetService(reference);
            }
        } catch (InvalidSyntaxException e) {
            this.eventLogger.log(DeployerLogEvents.WATCHED_REPOSITORIES_REFRESH_FAILED);
        }
        
    }
    
    @Override
    public DeploymentIdentity[] bulkDeploy(List<URI> arg0, DeploymentOptions arg1) throws DeploymentException {
        throw new UnsupportedOperationException();
    }
    
}
