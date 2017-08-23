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

package org.eclipse.virgo.medic.log.impl.config;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.virgo.medic.log.ConfigurationPublicationFailedException;
import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.eclipse.virgo.medic.log.LoggingConfigurationPublisher;
import org.osgi.framework.BundleContext;


public class StandardLoggingConfigurationPublisher implements LoggingConfigurationPublisher {

    private final BundleContext context;
    
    private static final String DEFAULT_CONFIGURATION_ID = "org.eclipse.virgo.medic";

    public StandardLoggingConfigurationPublisher(BundleContext context) {
        this.context = context;
    }

    public void publishConfiguration(File configuration, String id) throws ConfigurationPublicationFailedException {
        LoggingConfiguration loggingConfiguration = createLoggingConfiguration(configuration, id);
        Dictionary<String, String> properties = new Hashtable<String, String>();

        properties.put(ServiceRegistryConfigurationLocator.LOGGING_CONFIGURATION_ID_SERVICE_PROPERTY, id);
        context.registerService(LoggingConfiguration.class.getName(), loggingConfiguration, properties);
    }

    private static LoggingConfiguration createLoggingConfiguration(File configuration, String id) throws ConfigurationPublicationFailedException {
        try {
            return new StandardLoggingConfiguration(configuration, id);
        } catch (IOException ioe) {
            throw new ConfigurationPublicationFailedException("Failed to read the configuration from the file '" + configuration.getAbsolutePath()
                + "'.", ioe);
        }
    }

	public void publishDefaultConfiguration(File file) throws ConfigurationPublicationFailedException {
		publishConfiguration(file, DEFAULT_CONFIGURATION_ID);
	}
}
