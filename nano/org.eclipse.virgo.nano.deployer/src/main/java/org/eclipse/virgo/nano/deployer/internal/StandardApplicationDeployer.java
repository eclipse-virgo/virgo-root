/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.deployer.internal;

import static java.lang.Integer.parseInt;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.core.KernelConfig;
import org.eclipse.virgo.nano.deployer.NanoDeployerLogEvents;
import org.eclipse.virgo.nano.deployer.SimpleDeployer;
import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeployerConfiguration;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
import org.eclipse.virgo.nano.deployer.support.HotDeployerEnabler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardApplicationDeployer implements ApplicationDeployer {

    private static final String TOPIC_RECOVERY_COMPLETED = "org/eclipse/virgo/kernel/deployer/recovery/COMPLETED";

    private EventLogger eventLogger;

    private final List<SimpleDeployer> simpleDeployers = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KernelConfig kernelConfig;

    private HotDeployerEnabler hotDeployerEnabler = null;

    private BundleContext bundleContext;

    private BundleDeployer defaultDeployer;

    private EventAdmin eventAdmin;

    public void activate(ComponentContext context) {
        this.bundleContext = context.getBundleContext();
        this.defaultDeployer = new BundleDeployer(context.getBundleContext(), this.eventLogger);
        this.simpleDeployers.add(this.defaultDeployer);

        recoveryComplete();
        initialiseHotDeployer();

        // TODO register the deployer MBean when the management classes are factored out in a new bundle.
        // Deployer deployerMBean = new StandardDeployer(appDeployer);
    }

    public void deactivate(ComponentContext context) {
        if (this.hotDeployerEnabler != null) {
            this.hotDeployerEnabler.stopHotDeployer();
        }
    }

    private void initialiseHotDeployer() {
        int deployerTimeout = parseInt(this.kernelConfig.getProperty("deployer.timeout"));
        String pickupDirectory = this.kernelConfig.getProperty("deployer.pickupDirectory");
        int scanInterval = parseInt(this.kernelConfig.getProperty("deployer.scanIntervalMillis"));
        DeployerConfiguration deployerConfiguration = new StandardDeployerConfiguration(deployerTimeout, new File(pickupDirectory), scanInterval);
        this.hotDeployerEnabler = new HotDeployerEnabler(this, deployerConfiguration, this.eventLogger);
        this.hotDeployerEnabler.startHotDeployer();
    }

    @Override
    public DeploymentIdentity deploy(URI uri) {
        boolean isThereSuitableDeployer = false;
        for (SimpleDeployer deployer : this.simpleDeployers) {
            if (deployer.canServeFileType(getFileTypeFromUri(uri))) {
                isThereSuitableDeployer = true;
                if (!deployer.isDeployFileValid(new File(uri))) {
                    this.eventLogger.log(NanoDeployerLogEvents.NANO_INVALID_FILE);
                } else {
                    if (deployer.isDeployed(uri)) {
                        deployer.update(uri);
                    } else {
                        deployer.deploy(uri);
                    }
                }
            }
        }
        if (!isThereSuitableDeployer) {
            handleUnsupportedFileType(uri);
        }
        return null;
    }

    @Override
    public DeploymentIdentity[] bulkDeploy(List<URI> uris, DeploymentOptions options) {
        if (uris != null && !uris.isEmpty()) {
            installDeployables(uris);
            startDeployables(uris);
            return getDeploymentIdentities(uris);
        } else {
            this.logger.warn("Cannot perform bulk deploy operation of the given URIs list as it is either empty or null.");
            return null;
        }
    }

    private DeploymentIdentity[] getDeploymentIdentities(List<URI> uris) {
        List<DeploymentIdentity> accumulatedDIs = new ArrayList<>();
        for (URI uri : uris) {
            DeploymentIdentity di = getDeploymentIdentity(uri);
            if (di != null) {
                accumulatedDIs.add(di);
            }
        }
        return accumulatedDIs.toArray(new DeploymentIdentity[0]);
    }

    @Override
    public void undeploy(DeploymentIdentity deploymentIdentity) {
        if (this.bundleContext != null) {
            String symbolicName = deploymentIdentity.getSymbolicName();
            String version = deploymentIdentity.getVersion();
            List<Bundle> existingBundles = new ArrayList<>();

            for (Bundle bundle : this.bundleContext.getBundles()) {
                if (bundle.getSymbolicName().equals(symbolicName) && bundle.getVersion().toString().equals(version)) {
                    existingBundles.add(bundle);
                }
            }
            if (existingBundles.size() > 1) {
                this.logger.warn("Multiple bundles matching the marked for uninstall symbolicName-version pair. List of all matches: "
                    + Arrays.toString(existingBundles.toArray(new Bundle[0])));
                this.logger.warn("Uninstalling the last-installed matching bundle " + existingBundles.get(existingBundles.size() - 1).toString());
            }
            boolean isThereSuitableDeployer = false;
            for (SimpleDeployer deployer : this.simpleDeployers) {
                if (deployer.canServeFileType(deploymentIdentity.getType())) {
                    isThereSuitableDeployer = true;
                    deployer.undeploy(existingBundles.get(0));
                }
            }
            if (!isThereSuitableDeployer) {
                this.defaultDeployer.undeploy(existingBundles.get(0));
            }
        }
    }

    @Override
    public DeploymentIdentity getDeploymentIdentity(URI uri) {
        for (SimpleDeployer deployer : this.simpleDeployers) {
            if (deployer.canServeFileType(getFileTypeFromUri(uri))) {
                return deployer.getDeploymentIdentity(uri);
            }
        }
        return null;
    }

    @Override
    public boolean isDeployed(URI uri) {
        for (SimpleDeployer simpleDeployer : this.simpleDeployers) {
            if (simpleDeployer.canServeFileType(getFileTypeFromUri(uri))) {
                return simpleDeployer.isDeployed(uri) ^ simpleDeployer.isOfflineUpdated(uri);
            }
        }
        return false;
    }

    private String getFileTypeFromUri(URI uri) {
        String path = uri.toString();
        return path.substring(path.lastIndexOf(".") + 1);
    }

    @Override
    public DeploymentIdentity deploy(URI uri, DeploymentOptions options) {
        return this.deploy(uri);
    }

    @Override
    public DeploymentIdentity deploy(String type, String name, Version version) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity deploy(String type, String name, Version version, DeploymentOptions options) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public void undeploy(String applicationSymbolicName, String version) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity install(URI uri, DeploymentOptions options) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity install(URI uri) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public void undeploy(String type, String name, String version) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public void undeploy(DeploymentIdentity deploymentIdentity, boolean deleted) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity refresh(URI uri, String symbolicName) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public void refreshBundle(String bundleSymbolicName, String bundleVersion) {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    @Override
    public DeploymentIdentity[] getDeploymentIdentities() {
        throw new UnsupportedOperationException("Not supported in Virgo Nano.");
    }

    public void bindEventLogger(EventLogger logger) {
        this.eventLogger = logger;
    }

    public void unbindEventLogger(EventLogger logger) {
        this.eventLogger = null;
    }

    public void bindEventAdmin(EventAdmin admin) {
        this.eventAdmin = admin;
    }

    public void unbindEventAdmin(EventAdmin admin) {
        this.eventAdmin = null;
    }

    public void bindKernelConfig(KernelConfig config) {
        this.kernelConfig = config;
    }

    public void unbindKernelConfig(KernelConfig config) {
        this.kernelConfig = null;
    }

    public void bindSimpleDeployer(SimpleDeployer deployer) {
        this.simpleDeployers.add(deployer);
    }

    public void unbindSimpleDeployer(SimpleDeployer deployer) {
        this.simpleDeployers.remove(deployer);
    }

    private void handleUnsupportedFileType(URI uri) {
        List<String> acceptedTypes = new ArrayList<>();
        for (SimpleDeployer deployer : this.simpleDeployers) {
            acceptedTypes.addAll(deployer.getAcceptedFileTypes());
        }
        this.eventLogger.log(NanoDeployerLogEvents.NANO_UNRECOGNIZED_TYPE, uri, acceptedTypes);
    }

    private void installDeployables(List<URI> uris) {
        for (URI uri : uris) {
            boolean isThereSuitableDeployer = false;
            for (SimpleDeployer deployer : this.simpleDeployers) {
                if (deployer.canServeFileType(getFileTypeFromUri(uri))) {
                    isThereSuitableDeployer = true;
                    if (!deployer.isDeployFileValid(new File(uri))) {
                        this.eventLogger.log(NanoDeployerLogEvents.NANO_INVALID_FILE);
                    } else {
                        if (deployer.isDeployed(uri)) {
                            deployer.update(uri);
                        } else {
                            deployer.install(uri);
                        }
                    }
                }
            }
            if (!isThereSuitableDeployer) {
                handleUnsupportedFileType(uri);
            }
        }
    }

    private void startDeployables(List<URI> uris) {
        for (URI uri : uris) {
            boolean isThereSuitableDeployer = false;
            for (SimpleDeployer deployer : this.simpleDeployers) {
                if (deployer.canServeFileType(getFileTypeFromUri(uri))) {
                    isThereSuitableDeployer = true;
                    if (!deployer.isDeployFileValid(new File(uri))) {
                        this.eventLogger.log(NanoDeployerLogEvents.NANO_INVALID_FILE);
                    } else {
                        deployer.start(uri);
                    }
                }
            }
            if (!isThereSuitableDeployer) {
                handleUnsupportedFileType(uri);
            }
        }
    }

    private void recoveryComplete() {
        this.eventAdmin.postEvent(new Event(TOPIC_RECOVERY_COMPLETED, (Map<String, ?>) null));
    }

}
