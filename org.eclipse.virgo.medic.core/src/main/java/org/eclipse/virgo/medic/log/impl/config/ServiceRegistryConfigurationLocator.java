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

import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


public final class ServiceRegistryConfigurationLocator implements ConfigurationLocator {

    private final BundleContext bundleContext;

    static final String LOGGING_CONFIGURATION_ID_SERVICE_PROPERTY = "org.eclipse.virgo.medic.log.configuration.id";

    private static final String MEDIC_LOGGING_CONFIGURATION_HEADER = "Medic-LoggingConfiguration";

    public ServiceRegistryConfigurationLocator(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @SuppressWarnings("unchecked")
	public LoggingConfiguration locateConfiguration(Bundle bundle) {
        if (bundle != null) {
            String configurationName = (String) bundle.getHeaders().get(MEDIC_LOGGING_CONFIGURATION_HEADER);

            if (configurationName != null) {
                try {
					ServiceReference<LoggingConfiguration>[] serviceReferences = (ServiceReference<LoggingConfiguration>[]) this.bundleContext
							.getServiceReferences(LoggingConfiguration.class
									.getName(), "("
									+ LOGGING_CONFIGURATION_ID_SERVICE_PROPERTY
									+ "=" + configurationName + ")");
                    if (serviceReferences != null && serviceReferences.length > 0) {
                        ServiceReference<LoggingConfiguration> serviceReference = ServiceReferenceUtils.selectServiceReference(serviceReferences);
                        return (LoggingConfiguration) this.bundleContext.getService(serviceReference);
                    }
                } catch (InvalidSyntaxException ise) {

                }
            }
        }

        return null;
    }
}
