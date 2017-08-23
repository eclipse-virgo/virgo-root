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

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link ConfigurationListener} that notices {@link ConfigurationEvent#CM_UPDATED} and
 * {@link ConfigurationEvent#CM_DELETED} events to add and remove respectively {@link Artifact}s from the
 * {@link RuntimeArtifactRepository}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
final class ModelConfigurationListener implements ConfigurationListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RuntimeArtifactRepository artifactRepository;

    private final BundleContext bundleContext;

    private final ConfigurationAdmin configurationAdmin;

    private final Region independentRegion;

    public ModelConfigurationListener(@NonNull RuntimeArtifactRepository artifactRepository, @NonNull BundleContext bundleContext, @NonNull ConfigurationAdmin configurationAdmin, @NonNull Region independentRegion) {
        this.artifactRepository = artifactRepository;
        this.bundleContext = bundleContext;
        this.configurationAdmin = configurationAdmin;
        this.independentRegion = independentRegion;
    }

    /**
     * {@inheritDoc}
     */
    public void configurationEvent(ConfigurationEvent event) {
        if (ConfigurationEvent.CM_UPDATED == event.getType()) {
            processUpdate(event);
        } else if (ConfigurationEvent.CM_DELETED == event.getType()) {
            processDelete(event);
        }
    }

    private void processUpdate(ConfigurationEvent event) {
        logger.info("Processing update event for '{}'", event.getPid());
        this.artifactRepository.add(createArtifact(event));
    }

    private void processDelete(ConfigurationEvent event) {
        logger.info("Processing delete event for '{}'", event.getPid());

        Artifact artifact = this.artifactRepository.getArtifact(ConfigurationArtifact.TYPE, event.getPid(), Version.emptyVersion, independentRegion);
        if (artifact instanceof ConfigurationArtifact) {
            this.artifactRepository.remove(ConfigurationArtifact.TYPE, event.getPid(), Version.emptyVersion, independentRegion);
        }
    }

    private ConfigurationArtifact createArtifact(ConfigurationEvent event) {
        return new ConfigurationArtifact(this.bundleContext, this.configurationAdmin, event.getPid(), independentRegion);
    }

}
