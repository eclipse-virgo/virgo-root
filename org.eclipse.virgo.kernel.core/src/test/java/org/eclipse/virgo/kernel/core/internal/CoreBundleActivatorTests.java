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

package org.eclipse.virgo.kernel.core.internal;

import static org.easymock.EasyMock.createNiceMock;

import java.io.File;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventAdmin;


import org.eclipse.virgo.kernel.StubConfigurationAdmin;
import org.eclipse.virgo.kernel.core.Shutdown;
import org.eclipse.virgo.kernel.core.internal.CoreBundleActivator;
import org.eclipse.virgo.kernel.core.internal.StartupTracker;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.service.component.StubComponentContext;
import org.eclipse.virgo.teststubs.osgi.service.event.StubEventAdmin;

/**
 * Test the logic of {@link CoreBundleActivator}
 * 
 */
public class CoreBundleActivatorTests {

    @Test(expected = IllegalStateException.class)
    public void noConfigService() throws Exception {
        StubBundleContext bundleContext = new StubBundleContext();
        StubComponentContext componentContext = new StubComponentContext(bundleContext);
        bundleContext.addFilter(StartupTracker.APPLICATION_CONTEXT_FILTER, FrameworkUtil.createFilter(StartupTracker.APPLICATION_CONTEXT_FILTER));
        CoreBundleActivator activator = new TestCoreBundleActivator();
        activator.activate(componentContext);
    }

    @Test
    public void startAndStop() throws Exception {
        StubBundleContext bundleContext = new StubBundleContext();
        StubComponentContext componentContext = new StubComponentContext(bundleContext);
        DumpGenerator dumpGenerator = createNiceMock(DumpGenerator.class);
        bundleContext.addFilter(StartupTracker.APPLICATION_CONTEXT_FILTER, FrameworkUtil.createFilter(StartupTracker.APPLICATION_CONTEXT_FILTER));
        bundleContext.registerService(ConfigurationAdmin.class.getName(), new StubConfigurationAdmin(), null);
        bundleContext.registerService(EventLogger.class.getName(), new MockEventLogger(), null);
        bundleContext.registerService(EventAdmin.class.getName(), new StubEventAdmin(), null);
        bundleContext.registerService(DumpGenerator.class.getName(), dumpGenerator, null);
        bundleContext.addProperty("org.eclipse.virgo.kernel.domain", "test");
        bundleContext.addProperty("org.eclipse.virgo.kernel.home", new File(".").getAbsolutePath());

        CoreBundleActivator activator = new TestCoreBundleActivator();
        activator.activate(componentContext);
        activator.deactivate(componentContext);
    }
    
    private static final class TestCoreBundleActivator extends CoreBundleActivator {
        @Override
        protected Shutdown createShutdown(BundleContext context, EventLogger eventLogger) {
            return new Shutdown() {
                public void immediateShutdown() {                 
                }

                public void shutdown() {
                }                
            };
        }
        
    }
}
