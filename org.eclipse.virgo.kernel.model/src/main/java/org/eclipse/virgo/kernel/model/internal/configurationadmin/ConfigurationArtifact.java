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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.internal.AbstractArtifact;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link Artifact} that delegates to a Configuration Admin {@link Configuration}
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
final class ConfigurationArtifact extends AbstractArtifact {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static final String TYPE = "configuration";

    private final ConfigurationAdmin configurationAdmin;

    private final String pid;

    public ConfigurationArtifact(@NonNull BundleContext bundleContext, @NonNull ConfigurationAdmin configurationAdmin, @NonNull String pid, @NonNull Region region) {
        super(bundleContext, TYPE, pid, Version.emptyVersion, region);
        this.configurationAdmin = configurationAdmin;
        this.pid = pid;
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactState getState() {
        return ArtifactState.ACTIVE;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<String, String>(super.getProperties());
        try {
            Configuration configuration = this.configurationAdmin.getConfiguration(this.pid, null);
            properties.put("Pid", configuration.getPid());
            String factoryPid = configuration.getFactoryPid();
            if(factoryPid != null){
            	properties.put("Factory Pid", factoryPid);
            }
            String bundleLocation = configuration.getBundleLocation();
            if(bundleLocation != null){
            	properties.put("Bound to Bundle", bundleLocation);
            }
        } catch (IOException ignored) {
            // Default to superclass behaviour
        } catch (IllegalStateException e){
        	properties.put("ERROR", "Configuration has been deleted");
        }
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public boolean refresh() {
        return false;
        // Do nothing for a Configuration
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
        // Do nothing for a Configuration
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        // Do nothing for a Configuration
    }

    /**
     * {@inheritDoc}
     */
    public void uninstall() {
        try {
            this.configurationAdmin.getConfiguration(this.pid, null).delete();
        } catch (IOException e) {
            logger.error("Unable to delete configuration for '{}'", this.pid);
            throw new RuntimeException(String.format("Unable to delete configuration for '%s'", this.pid), e);
        }
    }

}
