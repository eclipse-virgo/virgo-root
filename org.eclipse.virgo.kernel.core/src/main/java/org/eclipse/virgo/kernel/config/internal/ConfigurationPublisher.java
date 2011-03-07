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

package org.eclipse.virgo.kernel.config.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.common.IterableEnumeration;
import org.eclipse.virgo.util.common.StringUtils;

/**
 * <code>ConfigurationPublisher</code>, publishes kernel configuration to {@link ConfigurationAdmin}.
 * <p/>
 * Properties files in {@link KernelConfiguration#getConfigDirectories() config directories} are read in and applied to
 * {@link Configuration Configurations} owned by <code>ConfigurationAdmin</code>. A file called
 * <code><i>name</i>.properties</code> results in a <Code>Configuration</code> with the service pid
 * <code><i>name</i></code>.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 * 
 */
final class ConfigurationPublisher {

    private final ConfigurationAdmin configAdmin;

    private final PropertiesSource[] sources;

    ConfigurationPublisher(ConfigurationAdmin configAdmin, PropertiesSource... sources) {
        this.configAdmin = configAdmin;
        this.sources = (sources == null ? new PropertiesSource[0] : sources);
    }

    void publishConfigurations() throws IOException {
        for (PropertiesSource source : this.sources) {
            Map<String, Properties> configurationProperties = source.getConfigurationProperties();
            if (configurationProperties != null) {
                for (Entry<String, Properties> entry : configurationProperties.entrySet()) {
                    populateConfigurationWithProperties(entry.getKey(), entry.getValue());
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void populateConfigurationWithProperties(@NonNull String pid, @NonNull Properties properties) throws IOException {
        Configuration config = getConfiguration(pid, properties);

        Dictionary configProperties = config.getProperties();
        if (configProperties == null) {
            configProperties = new Hashtable();
        }

        for (Object key : new IterableEnumeration(properties.keys())) {
            Object value = properties.get(key);
            configProperties.put(key, value);
        }

        config.update(configProperties);
    }

    /**
     * @param pid
     * @param properties
     * @return
     * @throws IOException 
     */
    private Configuration getConfiguration(String pid, Properties properties) throws IOException {
        Configuration result = null;
        String factoryPid = properties.getProperty(ConfigurationAdmin.SERVICE_FACTORYPID);
        if (StringUtils.hasText(factoryPid)) {
            result = this.configAdmin.createFactoryConfiguration(factoryPid, null);
        } else {
            result = this.configAdmin.getConfiguration(pid, null);
        }
        return result;
    }

}
