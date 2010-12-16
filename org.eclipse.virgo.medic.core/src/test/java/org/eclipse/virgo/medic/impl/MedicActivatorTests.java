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

import static org.eclipse.virgo.teststubs.osgi.framework.OSGiAssert.assertServiceListenerCount;
import static org.eclipse.virgo.teststubs.osgi.framework.OSGiAssert.assertServiceRegistrationCount;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.io.PrintStream;

import org.junit.Test;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.packageadmin.PackageAdmin;


import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
import org.eclipse.virgo.medic.impl.MedicActivator;
import org.eclipse.virgo.medic.log.DelegatingPrintStream;
import org.eclipse.virgo.medic.log.LoggingConfigurationPublisher;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.ObjectClassFilter;

@SuppressWarnings("deprecation")
public class MedicActivatorTests {

    @Test
    public void startAndStop() throws Exception {
        BundleActivator bundleActivator = new MedicActivator();
        StubBundleContext bundleContext = new StubBundleContext().addFilter(new ObjectClassFilter(DumpContributor.class));
        bundleContext.addProperty("org.eclipse.virgo.suppress.heap.dumps", "false");
        
        PackageAdmin packageAdmin = createNiceMock(PackageAdmin.class);
        replay(packageAdmin);
        
        bundleContext.registerService(PackageAdmin.class.getName(), packageAdmin, null);

        bundleActivator.start(bundleContext);
        assertServiceListenerCount(bundleContext, 1);
        assertServiceRegistrationCount(bundleContext, DumpGenerator.class, 1);
        assertServiceRegistrationCount(bundleContext, DumpContributor.class, 3);
        assertServiceRegistrationCount(bundleContext, EventLoggerFactory.class, 1);
        assertServiceRegistrationCount(bundleContext, EventLogger.class, 1);
        assertServiceRegistrationCount(bundleContext, LoggingConfigurationPublisher.class, 1);
        assertServiceRegistrationCount(bundleContext, PrintStream.class, 2);
        assertServiceRegistrationCount(bundleContext, DelegatingPrintStream.class, 2);
        assertServiceRegistrationCount(bundleContext, ConfigurationListener.class, 1);
        assertServiceRegistrationCount(bundleContext, BundleListener.class, 1);

        bundleActivator.stop(bundleContext);      
        
        assertEquals(1, bundleContext.getServiceRegistrations().size());
    }        

    @Test
    public void copeWithNullsDuringStop() throws Exception {
        BundleActivator bundleActivator = new MedicActivator();
        BundleContext context = new StubBundleContext(new StubBundle());
        bundleActivator.stop(context);
    }

}
