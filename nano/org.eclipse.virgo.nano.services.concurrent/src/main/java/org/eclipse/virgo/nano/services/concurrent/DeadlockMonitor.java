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

package org.eclipse.virgo.nano.services.concurrent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.nano.services.concurrent.DeadlockAnalyser.Deadlock;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.math.Sets;
import org.osgi.service.component.ComponentContext;

/**
 * Monitors all running {@link Thread Threads} and triggers a dump when a deadlock is detected.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class DeadlockMonitor {

    private static final int PERIOD = 10;

    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    private ScheduledExecutorService executorService;

    private DumpGenerator dumpGenerator;

    private EventLogger eventLogger;

    private volatile ScheduledFuture<?> future;

    public void activate(ComponentContext context) {
        this.executorService = new ScheduledThreadPoolExecutor(1);
        this.future = this.executorService.scheduleAtFixedRate(new DeadlockMonitorTask(this.dumpGenerator, this.eventLogger), PERIOD, PERIOD, UNIT);
    }
    
    public void deactivate(ComponentContext context) {
        if (this.executorService != null) {
            this.executorService.shutdown();
        }

        if (this.future != null) {
            this.future.cancel(true);
        }
    }
    
    public DeadlockMonitor() {
    }
    
    /**
     * Creates a new <code>DeadlockMonitor</code>.
     * 
     * @param executorService the <code>ScheduledExecutorService</code>
     * @param dumpGenerator the @{link {@link DumpGenerator} to trigger a dump.
     * @param eventLogger 
     */
    public DeadlockMonitor(DumpGenerator dumpGenerator, EventLogger eventLogger) {
        this.dumpGenerator = dumpGenerator;
        this.eventLogger = eventLogger;
    }
    
    public void bindEventLogger(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }
    public void unbindEventLogger(EventLogger eventLogger) {
        this.eventLogger = null;
    }
    
    public void bindDumpGenerator(DumpGenerator dumpGenerator) {
        this.dumpGenerator = dumpGenerator;
    }
    public void unbindDumpGenerator(DumpGenerator dumpGenerator) {
        this.dumpGenerator = null;
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
