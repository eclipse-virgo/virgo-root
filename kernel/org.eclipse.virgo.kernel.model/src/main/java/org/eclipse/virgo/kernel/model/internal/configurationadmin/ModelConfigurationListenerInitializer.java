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

package org.eclipse.virgo.kernel.model.internal.configurationadmin;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An initializer responsible for registering a {@link ModelConfigurationListener} and enumerating any existing
 * {@link Configuration} objects from {@link ConfigurationAdmin}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 * @see ModelConfigurationListener
 */
public final class ModelConfigurationListenerInitializer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();

    private final RuntimeArtifactRepository artifactRepository;

    private final BundleContext bundleContext;

    private final ConfigurationAdmin configurationAdmin;

    private final Region globalRegion;

    public ModelConfigurationListenerInitializer(@NonNull RuntimeArtifactRepository artifactRepository, @NonNull BundleContext bundleContext, @NonNull ConfigurationAdmin configurationAdmin, @NonNull Region globalRegion) {
        this.artifactRepository = artifactRepository;
        this.bundleContext = bundleContext;
        this.configurationAdmin = configurationAdmin;
        this.globalRegion = globalRegion;
    }

    /**
     * Registers a {@link ModelConfigurationListener} with the service registry. Enumerates any existing
     * {@link Configuration} objects that exist from {@link ConfigurationAdmin}.
     * 
     * @throws IOException
     * @throws InvalidSyntaxException
     */
    @PostConstruct
    public void initialize() throws IOException, InvalidSyntaxException {
        ModelConfigurationListener configurationListener = new ModelConfigurationListener(artifactRepository, bundleContext, configurationAdmin, globalRegion);
        this.registrationTracker.track(this.bundleContext.registerService(ConfigurationListener.class.getCanonicalName(), configurationListener, null));
        Configuration[] configurations = this.configurationAdmin.listConfigurations(null);
        if (configurations != null) {
            for (Configuration configuration : configurations) {
                try {
                    this.artifactRepository.add(new ConfigurationArtifact(bundleContext, configurationAdmin, configuration.getPid(), globalRegion));
                } catch (Exception e) {
                    logger.error(String.format("Exception adding configuration '%s' to the repository", configuration.getPid()), e);
                }
            }
        }
    }

    /**
     * Unregisters the listener from the service registry
     */
    @PreDestroy
    public void destroy() {
        this.registrationTracker.unregisterAll();
    }
}
