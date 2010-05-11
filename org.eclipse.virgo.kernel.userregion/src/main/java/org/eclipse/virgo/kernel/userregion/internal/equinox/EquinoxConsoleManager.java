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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import org.eclipse.osgi.framework.internal.core.BundleContextImpl;
import org.eclipse.osgi.framework.internal.core.FrameworkConsole;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.kernel.osgi.framework.OsgiConfiguration;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkLogEvents;

import org.eclipse.virgo.kernel.core.Shutdown;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.io.NetUtils;

/**
 * Manages the lifecycle of the Equinox OSGi console.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class EquinoxConsoleManager {

    private static final String CONSOLE_THREAD_NAME = "console-thread";

    private final Object monitor = new Object();

    private final BundleContext bundleContext;

    private final OsgiConfiguration configuration;

    private final Shutdown shutdown;

    private final EventLogger eventLogger;

    private FrameworkConsole console;

    private Thread consoleThread;

    public EquinoxConsoleManager(BundleContext bundleContext, OsgiConfiguration configuration, Shutdown shutdown, EventLogger eventLogger) {
        this.bundleContext = bundleContext;
        this.configuration = configuration;
        this.shutdown = shutdown;
        this.eventLogger = eventLogger;
    }

    public void start() {
        FrameworkConsole console = null;
        Thread consoleThread = null;
        if (this.configuration.isConsoleEnabled()) {
            BundleContextImpl bundleContext = (BundleContextImpl) this.bundleContext;
            int consolePort = this.configuration.getConsolePort();

            checkConsolePortAvailable(consolePort);

            console = new FrameworkConsole(bundleContext.getFramework(), consolePort, null);
            consoleThread = new Thread(console, CONSOLE_THREAD_NAME);
            consoleThread.setDaemon(true);
            consoleThread.start();

            this.eventLogger.log(OsgiFrameworkLogEvents.OSGI_CONSOLE_PORT, consolePort);
        }
        synchronized (this.monitor) {
            this.console = console;
            this.consoleThread = consoleThread;
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        FrameworkConsole console;
        Thread consoleThread;
        synchronized (this.monitor) {
            console = this.console;
            consoleThread = this.consoleThread;
            this.console = null;
            this.consoleThread = null;
        }
        if (console != null) {
            console.shutdown();
        }
        if (consoleThread != null) {
            consoleThread.stop();
        }
    }

    private void checkConsolePortAvailable(int consolePort) {
        if (!NetUtils.isPortAvailable(consolePort)) {
            this.eventLogger.log(OsgiFrameworkLogEvents.OSGI_CONSOLE_PORT_IN_USE, consolePort);
            this.shutdown.immediateShutdown();
        }
    }
}
