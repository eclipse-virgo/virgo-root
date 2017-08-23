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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;

import org.eclipse.virgo.nano.core.internal.ShutdownManager;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

public class ShutdownManagerTests {

    private final Framework framework = createMock(Framework.class);
    
    private final Runtime runtime = Runtime.getRuntime();

    private final MockEventLogger eventLogger = new MockEventLogger();

    private final StubBundle bundle = new StubBundle();

    private final StubBundleContext bundleContext = new StubBundleContext(bundle);

    @Test
    public void shutdown() throws BundleException, InterruptedException {

        expect(this.framework.getBundleContext()).andReturn(this.bundleContext).anyTimes();
        this.framework.stop();
        expect(this.framework.waitForStop(30000)).andReturn(new FrameworkEvent(FrameworkEvent.STOPPED, this.bundle, null));

        replay(this.framework);

        UnitTestShutdownManager shutdownManager = new UnitTestShutdownManager(this.eventLogger, this.framework, this.runtime);
        shutdownManager.shutdown();

        verify(this.framework);
        
        assertTrue(shutdownManager.isExited());
        assertFalse(shutdownManager.isHalted());
    }

    @Test
    public void failedShutdownDrivesImmediateShutdown() throws Exception {
        expect(this.framework.getBundleContext()).andReturn(this.bundleContext).anyTimes();
        this.framework.stop();
        expect(this.framework.waitForStop(30000)).andReturn(new FrameworkEvent(FrameworkEvent.WAIT_TIMEDOUT, this.bundle, null));

        replay(this.framework);

        UnitTestShutdownManager shutdownManager = new UnitTestShutdownManager(this.eventLogger, this.framework, this.runtime);
        shutdownManager.shutdown();

        verify(this.framework);

        assertTrue(this.eventLogger.isLogged("KE0012E"));
        assertFalse(shutdownManager.isExited());
        assertTrue(shutdownManager.isHalted());
        
        shutdownManager.removeShutdownHook(); // avoid error messages on the console
    }

    @Test
    public void shutdownMessageLogged() {
        expect(this.framework.getBundleContext()).andReturn(this.bundleContext).anyTimes();

        replay(this.framework);
        UnitTestShutdownManager shutdownManager = new UnitTestShutdownManager(this.eventLogger, this.framework, this.runtime);
        verify(this.framework);

        List<BundleListener> bundleListeners = this.bundleContext.getBundleListeners();

        assertEquals(1, bundleListeners.size());
        bundleListeners.get(0).bundleChanged(new BundleEvent(BundleEvent.STOPPING, this.bundle));

        assertTrue(this.eventLogger.isLogged("KE0010I"));
        assertFalse(shutdownManager.isExited());
        assertFalse(shutdownManager.isHalted());

    }

    private static final class UnitTestShutdownManager extends ShutdownManager {
        
        private volatile boolean exited = false;
        
        private volatile boolean halted = false;

        public UnitTestShutdownManager(EventLogger eventLogger, Framework framework, Runtime runtime) {
            super(eventLogger, framework, runtime);
        }

        @Override
        protected void exitJvm() {
            this.exited = true;
        }

        @Override
        protected void haltJvm(int status) {
            this.halted = true;
        }
        
        public boolean isExited() {
            return this.exited;
        }
        
        public boolean isHalted() {
            return this.halted;
        }
    }
    
}
