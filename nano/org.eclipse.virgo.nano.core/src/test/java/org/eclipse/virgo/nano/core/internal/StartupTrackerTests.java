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

package org.eclipse.virgo.nano.core.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.nano.config.internal.KernelConfiguration;
import org.eclipse.virgo.nano.core.Shutdown;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.service.event.StubEventAdmin;
import org.eclipse.virgo.test.stubs.support.ObjectClassFilter;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class StartupTrackerTests {

    private final StubBundleContext bundleContext = new StubBundleContext();

    private final StubEventAdmin eventAdmin = new StubEventAdmin();
    
    private final StubBundle bundle = new StubBundle("org.eclipse.virgo.kernel.startuptest", new Version(1,0,0));
    
    private final Shutdown shutdown = createMock(Shutdown.class);
    
    private final DumpGenerator dumpGenerator = createMock(DumpGenerator.class);
    
    @Before
    public void setup() {               
        this.bundleContext.addInstalledBundle(bundle);

        this.bundleContext.addProperty("org.eclipse.virgo.kernel.home", "build");
        this.bundleContext.addProperty("org.eclipse.virgo.kernel.domain", "the-domain");
        this.bundleContext.addFilter(new ObjectClassFilter("org.springframework.context.ApplicationContext"));
        this.bundleContext.registerService(EventAdmin.class.getName(), this.eventAdmin, null);
        
        this.bundle.setBundleContext(bundleContext);
    }

    @Test
    public void successfulStartup() throws Exception {
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(this.bundleContext);
        
		StartupTracker tracker = new StartupTracker(this.bundleContext, new KernelConfiguration(this.bundleContext), 30, bundleStartTracker, this.shutdown, this.dumpGenerator);
        tracker.start();

        assertTrue(this.eventAdmin.awaitPostingOfEvent(new Event("org/eclipse/virgo/kernel/STARTING", (Map<String, ?>)null), 10000));
        
        this.bundle.start();
        
        assertTrue(this.eventAdmin.awaitPostingOfEvent(new Event("org/eclipse/virgo/kernel/STARTED", (Map<String, ?>)null), 10000));

        tracker.stop();
    }

    @Test
    public void startupTimeout() {
    	
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(this.bundleContext);
        
        this.bundle.addHeader("Spring-Context", "foo");
        
        this.shutdown.immediateShutdown();
        this.dumpGenerator.generateDump("startupTimedOut");
        
        replay(this.shutdown, this.dumpGenerator);

        StartupTracker tracker = new StartupTracker(this.bundleContext, new KernelConfiguration(this.bundleContext), 1, bundleStartTracker, this.shutdown, this.dumpGenerator);
        tracker.start();

        assertTrue(this.eventAdmin.awaitPostingOfEvent(new Event("org/eclipse/virgo/kernel/STARTING", (Map<String, ?>)null), 10000));
        assertTrue(this.eventAdmin.awaitPostingOfEvent(new Event("org/eclipse/virgo/kernel/START_TIMED_OUT", (Map<String, ?>)null), 10000));

        waitForABit(500);
        
        tracker.stop();
        
        verify(this.shutdown);
        verify(this.dumpGenerator);
    }

    /**
     * This method is used to allow actions taken after the events are posted to complete
     * before making test verification checks. This is an artifact of the testing environment, and the waits
     * should be no more than about 500 ms.
     * @param milliSeconds time to wait
     */
    private void waitForABit(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException ignored) {
        }
    }
    
    @Test
    public void startupFailed() throws InterruptedException {
        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(this.bundleContext);
        
        this.bundle.addHeader("Spring-Context", "foo");
        
        this.shutdown.immediateShutdown();

        Exception failure = new Exception();
        this.dumpGenerator.generateDump("startupFailed", failure);
        
        replay(this.shutdown, this.dumpGenerator);

		StartupTracker tracker = new StartupTracker(this.bundleContext, new KernelConfiguration(this.bundleContext), 1, bundleStartTracker, this.shutdown, this.dumpGenerator);
        tracker.start();

        assertTrue(this.eventAdmin.awaitPostingOfEvent(new Event("org/eclipse/virgo/kernel/STARTING", (Map<String, ?>)null), 10000));
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("bundle", bundle);
        properties.put("exception", failure);
        
        bundleStartTracker.handleEvent(new Event("org/osgi/service/blueprint/container/FAILURE", properties));
        
        assertTrue(this.eventAdmin.awaitPostingOfEvent(new Event("org/eclipse/virgo/kernel/START_FAILED", (Map<String, ?>)null), 10000));

        waitForABit(500);

        tracker.stop();
        
        verify(this.shutdown, this.dumpGenerator);
    }


    @Test
    public void statusMBeanRegistration() throws Exception {
        assertMBeanNotRegistered();

        BundleStartTracker bundleStartTracker = new BundleStartTracker(new SyncTaskExecutor());
        bundleStartTracker.initialize(this.bundleContext);
        
        StartupTracker tracker = new StartupTracker(this.bundleContext, new KernelConfiguration(this.bundleContext), 1, bundleStartTracker, this.shutdown, this.dumpGenerator);
        tracker.start();

        assertMBeanRegistered();

        tracker.stop();

        assertMBeanNotRegistered();
    }

    private void assertMBeanRegistered() throws Exception {
        assertTrue(isMBeanRegistered());
    }

    private void assertMBeanNotRegistered() throws Exception {
        assertFalse(isMBeanRegistered());
    }

    private boolean isMBeanRegistered() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        return mBeanServer.isRegistered(new ObjectName("the-domain", "type", "KernelStatus"));
    }
}
