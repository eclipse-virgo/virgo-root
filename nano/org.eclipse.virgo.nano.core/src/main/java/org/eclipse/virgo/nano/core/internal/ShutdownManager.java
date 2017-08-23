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

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.nano.core.Shutdown;
import org.eclipse.virgo.nano.diagnostics.KernelLogEvents;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard implementation of {@link Shutdown} that may be called to initiate JVM shutdown. This class also listens for
 * <i>unsolicited</i> shutdown (that is, shutdown not initiated by this class) of the JVM or the OSGi framework and
 * reacts accordingly.
 * <p />
 * JVM shutdown may be initiated in either of two ways:
 * <ol>
 * <li>By this class (when a program calls one of the methods of the {@link Shutdown} interface). If graceful shutdown
 * is requested, this class shuts down the OSGi framework and then the JVM. If immediate shutdown is requested, this
 * class shuts down the JVM.</li>
 * <li>Not by this class. A {@link Runtime#addShutdownHook(Thread) shutdown hook} previously registered by this class
 * responds to JVM shutdown by shutting down the OSGi framework before allowing JVM shutdown to continue. Note that
 * {@link System#exit} must not be called under the shutdown hook as this can block indefinitely (see the javadoc of
 * {@link Runtime#exit}).</li>
 * </ol>
 * If this class attempts to shut down the OSGi framework but this takes longer than {@code SHUTDOWN_TIMOUT}, an error
 * message is written to the event log and the JVM is halted abruptly with a non-zero exit code.
 * <p />
 * When OSGi framework shutdown occurs, a synchronous bundle listener previously registered by this class unregisters
 * the shutdown hook (unless this class initiated the OSGi framework shutdown in response to an unsolicited JVM
 * shutdown, in which case the shutdown hook is left in place).</li>
 * <p />
 * This class expects some recursive invocations and avoids others:
 * <ul>
 * <li>A graceful shutdown request on the {@link Shutdown} interface normally results in the synchronous bundle listener
 * being driven on OSGi framework shutdown.</li>
 * <li>An immediate shutdown request on the {@link Shutdown} interface unregisters the shutdown hook so that it is not
 * driven when the JVM is shut down.</li>
 * <li>An unsolicited JVM termination prevents the shutdown hook from being unregistered.</li>
 * </ul>
 * <p />
 * So, in summary, the JVM may terminate in the following ways:
 * <ol>
 * <li>Solicited graceful shutdown, which attempts to stop the OSGi framework and, if successful, exits the JVM.</li>
 * <li>Unsolicited graceful shutdown, which attempts to stop the OSGi framework and, if successful, allows JVM
 * termination to continue.</li>
 * <li>Solicited immediate shutdown, which exits the JVM.</li>
 * <li>Solicited halt if an attempt by this class to stop the OSGi framework fails or times out.</li>
 * <li>Unsolicited halt or other abrupt termination (such as "kill -9" or a power failure), which does not involve this
 * class.</li>
 * </ol>
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
class ShutdownManager implements Shutdown {

    private static final int NORMAL_TERMINATION_EXIT_CODE = 0;

    private static final int GRACEFUL_TERMINATION_FAILURE_EXIT_CODE = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownManager.class);

    private static final long SHUTDOWN_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private static final int STATE_RUNNING = 0;

    private static final int STATE_STOPPING = 1;

    private final AtomicInteger state = new AtomicInteger(STATE_RUNNING);

    private final EventLogger eventLogger;

    private final Framework framework;

    private final Runtime runtime;

    private final Thread shutdownHook = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                // set to stopping so we don't remove the shutdown hook later
                if (ShutdownManager.this.compareAndSetHookStopping()) {
                    ShutdownManager.this.doShutdown(false);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    });

    public ShutdownManager(EventLogger eventLogger, Framework framework, Runtime runtime) {
        this.eventLogger = eventLogger;
        this.framework = framework;
        this.runtime = runtime;
        runtime.addShutdownHook(this.shutdownHook);
        BundleContext bundleContext = framework.getBundleContext();
        bundleContext.addBundleListener(new ShutdownLoggingListener());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        doShutdown(true);
    }

    private void doShutdown(boolean solicitedShutdown) {
        FrameworkEvent shutdownResponse = null;
        try {
            this.framework.stop();
            shutdownResponse = this.framework.waitForStop(SHUTDOWN_TIMEOUT);
        } catch (BundleException ex) {
            LOGGER.error("Error during shutdown.", ex);
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted during shutdown.", ex);
        }

        if (isSuccessfulStopResponse(shutdownResponse)) {
            // If this class initiated shutdown, shut down the JVM. Otherwise allow JVM termination to continue.
            if (solicitedShutdown) {
                initiateJvmTermination();
            }
        } else {
            // Escalate to JVM halt.
            this.eventLogger.log(KernelLogEvents.SHUTDOWN_HALTED);
            haltJvm(GRACEFUL_TERMINATION_FAILURE_EXIT_CODE);
        }
    }

    private void initiateJvmTermination() {
        removeShutdownHook();
        exitJvm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void immediateShutdown() {
        this.eventLogger.log(KernelLogEvents.IMMEDIATE_SHUTDOWN_INITIATED);
        initiateJvmTermination();
    }

    /**
     * This method must not be overridden except by testcases.
     */
    protected void exitJvm() {
        System.exit(NORMAL_TERMINATION_EXIT_CODE);
    }

    /**
     * This method must not be overridden except by testcases.
     */
    protected void haltJvm(int status) {
        this.runtime.halt(status);
    }

    private boolean isSuccessfulStopResponse(FrameworkEvent shutdownResponse) {
        return shutdownResponse != null && shutdownResponse.getType() == FrameworkEvent.STOPPED;
    }

    /**
     * This method must only be called by testcases.
     */
    final void removeShutdownHook() {
        if (compareAndSetHookStopping()) {
            this.runtime.removeShutdownHook(this.shutdownHook);
        }
    }

    private boolean compareAndSetHookStopping() {
        return this.state.compareAndSet(STATE_RUNNING, STATE_STOPPING);
    }

    private final class ShutdownLoggingListener implements SynchronousBundleListener {

        @Override
        public void bundleChanged(BundleEvent event) {
            BundleContext bundleContext = ShutdownManager.this.framework.getBundleContext();
            if (BundleEvent.STOPPING == event.getType() && event.getBundle() == bundleContext.getBundle()) {
                ShutdownManager.this.eventLogger.log(KernelLogEvents.SHUTDOWN_INITIATED);
                removeShutdownHook();
            }
        }
    }

}
