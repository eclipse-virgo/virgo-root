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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;

/**
 * Extension of {@link ThreadPoolExecutor} that handles kernel thread decoration.
 * <p/>
 * The implementation of ThreadPoolExecutor changed in Java 7 so that getCompletedTaskCount includes tasks that failed
 * with an exception, unlike Java 6 which excludes such tasks from the count. {@link KernelThreadPoolExecutor} class
 * inherits this Java version specific behaviour.
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Thread-safe.
 * 
 */
public final class KernelThreadPoolExecutor extends ThreadPoolExecutor implements KernelExecutorService {

    private final ExecutorServiceDelegate delegate;

    private final String poolName;

    /**
     * Creates a new <code>ServerThreadPoolExecutor</code>.
     * 
     * @param corePoolSize the core size of the pool.
     * @param maximumPoolSize the maximum size of the pool.
     * @param keepAliveTime the thread keep alive time.
     * @param unit the {@link TimeUnit} for the keep alive time.
     * @param workQueue the work queue.
     * @param poolName the name of the thread pool.
     * @param tracingService
     */
    public KernelThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
        String poolName, TracingService tracingService) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, poolName, tracingService, null);
    }

    /**
     * Creates a new <code>ServerThreadPoolExecutor</code>.
     * 
     * @param corePoolSize the core size of the pool.
     * @param maximumPoolSize the maximum size of the pool.
     * @param keepAliveTime the thread keep alive time.
     * @param unit the {@link TimeUnit} for the keep alive time.
     * @param workQueue the work queue.
     * @param poolName the name of the thread pool.
     * @param tracingService the kernel tracing service.
     * @param handler the rejected execution handler. May be <code>null</code>.
     */
    public KernelThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
        String poolName, TracingService tracingService, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, createThreadFactory(poolName), determineHandler(handler));
        this.poolName = poolName;
        this.delegate = new ExecutorServiceDelegate(tracingService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable command) {
        Runnable decorated = this.delegate.decorate(command);
        super.execute(decorated);
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
