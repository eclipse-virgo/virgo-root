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

package org.eclipse.virgo.kernel.services.concurrent.monitor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.kernel.services.concurrent.diagnostics.ConcurrentLogEvents;
import org.eclipse.virgo.kernel.services.concurrent.monitor.DeadlockAnalyser.Deadlock;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.math.Sets;

/**
 * Monitors all running {@link Thread Threads} and triggers a dump when a deadlock is detected.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class DeadlockMonitor {

    private static final int PERIOD = 10;

    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    private final ScheduledExecutorService executorService;

    private final DumpGenerator dumpGenerator;

    private final EventLogger eventLogger;

    private volatile ScheduledFuture<?> future;

    /**
     * Creates a new <code>DeadlockMonitor</code>.
     * 
     * @param executorService the <code>ScheduledExecutorService</code>
     * @param dumpGenerator the @{link {@link DumpGenerator} to trigger a dump.
     * @param eventLogger 
     */
    public DeadlockMonitor(ScheduledExecutorService executorService, DumpGenerator dumpGenerator, EventLogger eventLogger) {
        this.executorService = executorService;
        this.dumpGenerator = dumpGenerator;
        this.eventLogger = eventLogger;
    }

    /**
     * Starts the deadlock monitor.
     */
    public void start() {
        this.future = this.executorService.scheduleAtFixedRate(new DeadlockMonitorTask(this.dumpGenerator, this.eventLogger), PERIOD, PERIOD, UNIT);
    }

    /**
     * Stops the deadlock monitor.
     */
    public void stop() {
        if (this.executorService != null) {
            this.executorService.shutdown();
        }

        if (this.future != null) {
            this.future.cancel(true);
        }
    }

    /**
     * Task for monitoring threads for deadlocks.
     * <p/>
     */
    private static class DeadlockMonitorTask implements Runnable {

        private final DeadlockAnalyser analyser = new DeadlockAnalyser();

        private final EventLogger eventLogger;

        private final Set<Deadlock> lastSeenDeadlocks = new HashSet<Deadlock>();

        private final Object monitor = new Object();

        private final DumpGenerator dumpGenerator;

        /**
         * Creates a new <code>DeadlockMonitorTask</code>.
         * 
         * @param dumpGenerator the {@link DumpGenerator} to use.
         * @param eventLogger 
         */
        public DeadlockMonitorTask(DumpGenerator dumpGenerator, EventLogger eventLogger) {
            this.dumpGenerator = dumpGenerator;
            this.eventLogger = eventLogger;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            synchronized (this.monitor) {
                Deadlock[] deadlocks = this.analyser.findDeadlocks();
                if (deadlocks != null && deadlocks.length > 0) {
                    Set<Deadlock> asSet = Sets.asSet(deadlocks);
                    if (!asSet.equals(this.lastSeenDeadlocks)) {
                        this.eventLogger.log(ConcurrentLogEvents.DEADLOCK_DETECTED);
                        this.dumpGenerator.generateDump("deadlock");
                        this.lastSeenDeadlocks.clear();
                        this.lastSeenDeadlocks.addAll(asSet);
                    }
                } else {
                    this.lastSeenDeadlocks.clear();
                }
            }
        }

    }
}
