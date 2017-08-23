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

package org.eclipse.virgo.kernel.services.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;


/**
 * Delegate object that encapsulates common operations for {@link ExecutorService} implementations.
 * <p/>
 * Each {@link ExecutorService} should maintain its own instance of <code>ExecutorServiceDelegate</code>.
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Threadsafe.
 * 
 */
final class ExecutorServiceDelegate {

    private final ApplicationNameAccessor accessor;

    private final Object monitor = new Object();

    private long totalExecutionTime;

    public ExecutorServiceDelegate(TracingService tracingService) {
        this.accessor = new ApplicationNameAccessor(tracingService);
    }

    /**
     * Gets an estimate of the average amount of time spent processing successful tasks.
     * @param completedTaskCount 
     * @return the estimate of time spent.
     * @see ThreadPoolExecutor#getCompletedTaskCount()
     */
    public long getAverageExecutionTime(long completedTaskCount) {
        synchronized (this.monitor) {
            return completedTaskCount == 0 ? this.totalExecutionTime : this.totalExecutionTime / completedTaskCount;
        }
    }

    /**
     * Gets an estimate of the total amount of time spent processing successful tasks.
     * 
     * @return the estimate of time spent.
     * @see ThreadPoolExecutor#getCompletedTaskCount()
     */
    public long getExecutionTime() {
        synchronized (this.monitor) {
            return this.totalExecutionTime;
        }
    }

    /**
     * Creates a {@link Runnable} wrapper that gathers execution statistics for the supplied {@link Runnable}.
     * 
     * @param delegate the <code>Runnable</code> to gather the statistics for.
     * @return the wrapper.
     */
    public Runnable decorate(Runnable delegate) {
        return new KernelRunnable(delegate);
    }

    /**
     * Simple {@link Runnable} that tracks execution statistics for another, wrapped <code>Runnable</code> instance.
     * <p/>
     * 
     * <strong>Concurrent Semantics</strong><br/>
     * 
     * Threadsafe.
     * 
     */
    private class KernelRunnable implements Runnable {

        private final Runnable delegate;

        private final String applicationName;

        /**
         * @param delegate
         */
        public KernelRunnable(Runnable delegate) {
            this.delegate = delegate;
            this.applicationName = accessor.getCurrentApplicationName();
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            long timeBefore = System.currentTimeMillis();
            accessor.setCurrentApplicationName(this.applicationName);
            try {
                this.delegate.run();
            } finally {
                accessor.setCurrentApplicationName(null);
                ExecutorServiceDelegate outer = ExecutorServiceDelegate.this;
                long time = System.currentTimeMillis() - timeBefore;
                synchronized (outer.monitor) {
                    outer.totalExecutionTime += time;
                }
            }
        }
    }

    /**
     * Wrapper around {@link TracingService} that handles the service proxy disappearing.
     * <p/>
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Threadsafe.
     * 
     */
    private static class ApplicationNameAccessor {

        private final TracingService tracingService;

        public ApplicationNameAccessor(TracingService tracingService) {
            this.tracingService = tracingService;
        }

        /**
         * @return application name
         * @see TracingService#getCurrentApplicationName()
         */
        public String getCurrentApplicationName() {
            return this.tracingService.getCurrentApplicationName();
        }

        /**
         * @param applicationName 
         * @see TracingService#setCurrentApplicationName(String)
         */
        public void setCurrentApplicationName(String applicationName) {
            this.tracingService.setCurrentApplicationName(applicationName);
        }
    }
}
