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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.eclipse.virgo.nano.core.Shutdown;
import org.eclipse.virgo.nano.diagnostics.KernelLogEvents;
import org.eclipse.virgo.medic.eventlog.EventLogger;

/**
 * Standard implementation of {@link Shutdown} that performs all shutdown actions synchronously.
 * <p />
 * This implementation registers a {@link Runtime#addShutdownHook(Thread) shutdown hook} to perform graceful shutdown
 * when the user kills the VM.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
class ShutdownManager implements Shutdown {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownManager.class);

    private static final long SHUTDOWN_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private static final int STATE_RUNNING = 0;

    private static final int STATE_STOPPING = 1;

    private final AtomicInteger state = new AtomicInteger(STATE_RUNNING);

    private final EventLogger eventLogger;

    private final Framework framework;

    private final Thread shutdownHook = new Thread(new Runnable() {

        public void run() {
            try {
                // set to stopping so we don't try to remove the shutdown hook later
                state.set(STATE_STOPPING);
                ShutdownManager.this.shutdown();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    });

    public ShutdownManager(EventLogger eventLogger, Framework framework) {
        this.eventLogger = eventLogger;
        this.framework = framework;
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
        BundleContext bundleContext = framework.getBundleContext();
        bundleContext.addBundleListener(new ShutdownLoggingListener());
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() {
        FrameworkEvent shutdownResponse = null;
        try {
            this.framework.stop();
            shutdownResponse = this.framework.waitForStop(SHUTDOWN_TIMEOUT);
        } catch (BundleException ex) {
            LOGGER.error("Error during shutdown.", ex);
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted during shutdown.", ex);
        }

        if (!isSuccessfulStopResponse(shutdownResponse)) {
            immediateShutdown();
        } else {
        	removeShutdownHook();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void immediateShutdown() {
        removeShutdownHook();
        this.eventLogger.log(KernelLogEvents.IMMEDIATE_SHUTDOWN_INITIATED);
        exitVM();
    }
    
    protected void exitVM() {
    	System.exit(0);
    }

    private boolean isSuccessfulStopResponse(FrameworkEvent shutdownResponse) {
        return shutdownResponse != null && shutdownResponse.getType() == FrameworkEvent.STOPPED;
    }

    private void removeShutdownHook() {
        if(this.state.compareAndSet(STATE_RUNNING, STATE_STOPPING)) {
            Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
        }
    }

    private final class ShutdownLoggingListener implements SynchronousBundleListener {

        public void bundleChanged(BundleEvent event) {
            BundleContext bundleContext = ShutdownManager.this.framework.getBundleContext();
            if (BundleEvent.STOPPING == event.getType() && event.getBundle() == bundleContext.getBundle()) {
                ShutdownManager.this.eventLogger.log(KernelLogEvents.SHUTDOWN_INITIATED);
                removeShutdownHook();
            }
        }
    }

}
