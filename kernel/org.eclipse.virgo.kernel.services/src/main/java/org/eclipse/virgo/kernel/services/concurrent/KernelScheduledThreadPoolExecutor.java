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

import static org.eclipse.virgo.kernel.services.concurrent.ThreadPoolUtils.createThreadFactory;
import static org.eclipse.virgo.kernel.services.concurrent.ThreadPoolUtils.determineHandler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;


/**
 * Extension of {@link ScheduledThreadPoolExecutor} that handles kernel thread decoration.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class KernelScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements KernelScheduledExecutorService {

    private final ExecutorServiceDelegate delegate;

    private final String poolName;

    /**
     * Creates a new <code>KernelScheduledThreadPoolExecutor</code>.
     * 
     * @param corePoolSize the number of threads in the pool.
     * @param poolName the name of the pool.
     * @param tracingService the kernel tracing service
     * @see ScheduledThreadPoolExecutor#ScheduledThreadPoolExecutor(int, RejectedExecutionHandler)
     */
    public KernelScheduledThreadPoolExecutor(int corePoolSize, String poolName, TracingService tracingService) {
        this(corePoolSize, poolName, tracingService, null);
    }
    
    /**
     * Creates a new <code>KernelScheduledThreadPoolExecutor</code>.
     * 
     * @param corePoolSize the number of threads in the pool.
     * @param poolName the name of the pool.
     * @param tracingService the kernel tracing service
     * @param handler the {@link RejectedExecutionHandler}.
     * @see ScheduledThreadPoolExecutor#ScheduledThreadPoolExecutor(int, RejectedExecutionHandler)
     */
    public KernelScheduledThreadPoolExecutor(int corePoolSize, String poolName, TracingService tracingService, RejectedExecutionHandler handler) {
        super(corePoolSize, createThreadFactory(poolName), determineHandler(handler));
        this.poolName = poolName;
        this.delegate = new ExecutorServiceDelegate(tracingService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable command) {
        super.execute(this.delegate.decorate(command));
    }

    /**
     * Gets an estimate of the average amount of time spent processing successful tasks.
     * 
     * @return the estimate of time spent.
     * @see ThreadPoolExecutor#getCompletedTaskCount()
     */
    public long getAverageExecutionTime() {
        return this.delegate.getAverageExecutionTime(getCompletedTaskCount());
    }

    /**
     * Gets an estimate of the total amount of time spent processing successful tasks.
     * 
     * @return the estimate of time spent.
     * @see ThreadPoolExecutor#getCompletedTaskCount()
     */
    public long getExecutionTime() {
        return this.delegate.getExecutionTime();
    }

    /**
     * Get the unique name of the Pool used in this executor service
     * 
     * {@inheritDoc}
     */
    public String getPoolName() {
        return this.poolName;
    }
}
