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

import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.config.internal.commandline.CommandLinePropertiesSource;
import org.eclipse.virgo.nano.config.internal.ovf.OvfPropertiesSource;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * ConfigurationInitialiser
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * threadsafe
 *  
 */
public final class ConfigurationInitialiser {

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();
        
    private ConsoleConfigurationConvertor consoleConfigurationConvertor;

    public KernelConfiguration start(BundleContext context, EventLogger eventLogger) throws IOException {

        //The ConfigurationAdmin service is already available because the caller of this method(CoreBundleActivator.activate)
        //is a DS component that statically requires it
        ServiceReference<ConfigurationAdmin> configurationAdminReference = context.getServiceReference(ConfigurationAdmin.class);

        ConfigurationAdmin configAdmin = null;

        if (configurationAdminReference != null) {
            configAdmin = (ConfigurationAdmin) context.getService(configurationAdminReference);
        }

        if (configAdmin == null) {
            throw new IllegalStateException("ConfigurationAdmin service missing");
        }
        KernelConfiguration configuration = new KernelConfiguration(context);

        publishConfiguration(context, eventLogger, configuration, configAdmin);
        initializeDumpContributor(context, configAdmin);
        initializeConsoleConfigurationConvertor(context, configAdmin);
        return configuration;

    }

    private void publishConfiguration(BundleContext context, EventLogger eventLogger, KernelConfiguration configuration,
        ConfigurationAdmin configAdmin) throws IOException {
        PropertiesSource[] sources = new PropertiesSource[] { new UserConfigurationPropertiesSource(configuration.getConfigDirectories()),
            new OvfPropertiesSource(context, eventLogger), new KernelConfigurationPropertiesSource(configuration),
            new CommandLinePropertiesSource(context, eventLogger) };
        ConfigurationPublisher configPublisher = new ConfigurationPublisher(configAdmin, sources);
        configPublisher.publishConfigurations();
        configPublisher.registerConfigurationExporterService(context);
    }

    private void initializeDumpContributor(BundleContext context, ConfigurationAdmin configAdmin) {
        ConfigurationAdminDumpContributor dumpContributor = new ConfigurationAdminDumpContributor(configAdmin);
        this.tracker.track(context.registerService(DumpContributor.class.getName(), dumpContributor, null));
    }
    
    private void initializeConsoleConfigurationConvertor(BundleContext context, ConfigurationAdmin configAdmin) {
        consoleConfigurationConvertor = new ConsoleConfigurationConvertor(context, configAdmin);
        consoleConfigurationConvertor.start();
    }

    public void stop() {
        this.tracker.unregisterAll();
        consoleConfigurationConvertor.stop();
    }
}
