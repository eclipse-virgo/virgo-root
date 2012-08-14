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

package org.eclipse.virgo.nano.config.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.nano.core.ConfigurationExporter;
import org.eclipse.virgo.nano.serviceability.NonNull;
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

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    private final ConfigurationAdmin configAdmin;

    private final PropertiesSource[] sources;

    private final static String KERNEL_REGION_CONFIGURATION_PID = KernelConfigurationPropertiesSource.KERNEL_CONFIGURATION_PID;
    
    private final static String USER_REGION_CONFIGURATION_PID = "org.eclipse.virgo.kernel.userregion";

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
    
    void registerConfigurationExporterService(@NonNull BundleContext context) throws IOException {
    	ConfigurationExporter configurationExporter = createConfigurationExporterService();
        context.registerService(ConfigurationExporter.class, configurationExporter, null);
    }

	private ConfigurationExporter createConfigurationExporterService() throws IOException {
		Configuration kernelregionConfiguration = this.configAdmin.getConfiguration(KERNEL_REGION_CONFIGURATION_PID);
    	Configuration userregionConfiguration = this.configAdmin.getConfiguration(USER_REGION_CONFIGURATION_PID);
    	
        ConfigurationExporter configurationExporter = new StandardConfigurationExporter(userregionConfiguration, kernelregionConfiguration);
		return configurationExporter;
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
        try {
        	config.update(configProperties);
        } catch (RuntimeException e) {
			LOGGER.error(String.format("Failed to update configuration for pid '%s'", pid), e);
		}
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
