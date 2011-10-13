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

package org.eclipse.virgo.medic.impl;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.virgo.medic.impl.config.ConfigurationAdminConfigurationProvider;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.eclipse.virgo.medic.log.osgi.OSGiLogServiceListener;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.log.LogService;
import org.slf4j.LoggerFactory;

public final class MedicActivator implements BundleActivator {

    private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();

    private volatile ServiceReference<ExtendedLogReaderService> logReaderReference;

    private LogController logController;

    private ConfigurationProvider configurationProvider;

    public void start(BundleContext context) throws Exception {
        this.configurationProvider = new ConfigurationAdminConfigurationProvider(context);
        this.registrationTracker.track(context.registerService(ConfigurationListener.class.getName(), configurationProvider, null));

        this.logController = new LogController(context, configurationProvider, registrationTracker);

        configurationProvider.addChangeListener(logController);

        logController.logStart();
        logController.eventLogStart();
        logController.dumpStart();

        this.logReaderReference = context.getServiceReference(ExtendedLogReaderService.class);
        ExtendedLogReaderService logReader = context.getService(this.logReaderReference);
        logReader.addLogListener(new OSGiLogServiceListener(LoggerFactory.getLogger(LogService.class)));
    }

    public void stop(BundleContext context) throws Exception {
        this.registrationTracker.unregisterAll();
        ServiceReference<ExtendedLogReaderService> localLogReaderReference = this.logReaderReference;
        if (localLogReaderReference != null) {
            context.ungetService(localLogReaderReference);
        }

        if (configurationProvider != null) {
            configurationProvider.removeChangeListener(logController);
        }

        if (logController != null) {
            logController.dumpStop();
            logController.logStop();
        }
    }

}
