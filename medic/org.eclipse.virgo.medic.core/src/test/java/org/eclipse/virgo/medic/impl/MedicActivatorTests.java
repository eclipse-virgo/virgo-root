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

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.eclipse.virgo.test.stubs.framework.OSGiAssert.assertServiceListenerCount;
import static org.eclipse.virgo.test.stubs.framework.OSGiAssert.assertServiceRegistrationCount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
import org.eclipse.virgo.medic.log.DelegatingPrintStream;
import org.eclipse.virgo.medic.log.LoggingConfigurationPublisher;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.support.ObjectClassFilter;
import org.junit.Test;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;
import org.osgi.service.cm.ConfigurationListener;


public class MedicActivatorTests {
	
    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    @Test
    public void startAndStop() throws Exception {
        BundleActivator bundleActivator = new MedicActivator();
        StubBundleContext bundleContext = new StubBundleContext().addFilter(new ObjectClassFilter(DumpContributor.class));
        bundleContext.addProperty("org.eclipse.virgo.suppress.heap.dumps", "false");
        bundleContext.addProperty("org.eclipse.virgo.kernel.home", "src/test/resources/testDumps");
        
        ExtendedLogReaderService logReaderService = createNiceMock(ExtendedLogReaderService.class);
        replay(logReaderService);
        
        bundleContext.registerService(ExtendedLogReaderService.class, logReaderService, null);

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

        assertTrue(this.server.isRegistered(new ObjectName("org.eclipse.virgo.kernel:type=Medic,name=DumpInspector")));
        
        bundleActivator.stop(bundleContext);
        
        assertFalse(this.server.isRegistered(new ObjectName("org.eclipse.virgo.kernel:type=Medic,name=DumpInspector")));
        
        assertEquals(2, bundleContext.getServiceRegistrations().size());
    }        

    @Test
    public void copeWithNullsDuringStop() throws Exception {
        BundleActivator bundleActivator = new MedicActivator();
        BundleContext context = new StubBundleContext(new StubBundle());
        
        ExtendedLogReaderService logReaderService = createNiceMock(ExtendedLogReaderService.class);
        replay(logReaderService);
        context.registerService(ExtendedLogReaderService.class, logReaderService, null);
        
        bundleActivator.stop(context);
    }

}
