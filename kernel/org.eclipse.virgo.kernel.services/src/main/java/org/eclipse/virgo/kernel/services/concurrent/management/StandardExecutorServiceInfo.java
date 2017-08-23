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

package org.eclipse.virgo.kernel.services.concurrent.management;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.virgo.kernel.services.concurrent.ExecutorServiceInfo;
import org.eclipse.virgo.kernel.services.concurrent.ExecutorServiceStatistics;


/**
 * Standard implementation of {@link ExecutorServiceInfo}. Maintains a {@link WeakReference} to the underlying
 * {@link ExecutorServiceStatistics} to prevent pinning the executor in memory.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public class StandardExecutorServiceInfo implements ExecutorServiceInfo {

    private final Reference<ExecutorServiceStatistics> managedExecutorService;

    /**
     * Constructor that deals with figuring out what kind of service there is and setting the references accordingly.
     * 
     * @param service the {@link ExecutorServiceStatistics} being exposed.
     */
    public StandardExecutorServiceInfo(ExecutorServiceStatistics service) {
        this.managedExecutorService = new WeakReference<ExecutorServiceStatistics>(service);
    }

    /**
     * {@inheritDoc}
     */
    public long getAverageExecutionTime() {
        ExecutorServiceStatistics executorService = this.managedExecutorService.get();
        return executorService == null ? -1 : executorService.getAverageExecutionTime();
    }

    /**
     * {@inheritDoc}
     */
    public long getExecutionTime() {
        ExecutorServiceStatistics executorService = this.managedExecutorService.get();
        return executorService == null ? -1 : executorService.getExecutionTime();
    }

    /**
     * {@inheritDoc}
     */
    public int getPoolSize() {
        ExecutorServiceStatistics executorService = this.managedExecutorService.get();
        return executorService == null ? -1 : executorService.getPoolSize();
    }

    /**
     * {@inheritDoc}
     */
    public int getLargestPoolSize() {
        ExecutorServiceStatistics executorService = this.managedExecutorService.get();
        return executorService == null ? -1 : executorService.getLargestPoolSize();
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeName() {
        ExecutorServiceStatistics executorService = this.managedExecutorService.get();
        if(executorService == null) {
            return null;
        } else {
            return (executorService instanceof ScheduledExecutorService ? ScheduledExecutorService.class.getSimpleName() : ExecutorService.class.getSimpleName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getActiveCount() {
        ExecutorServiceStatistics executorService = this.managedExecutorService.get();
        return executorService == null ? -1 : executorService.getActiveCount();
    }

    /**
     * {@inheritDoc}
     */
    public int getMaximumPoolSize() {
        ExecutorServiceStatistics executorService = this.managedExecutorService.get();
        return executorService == null ? -1 : executorService.getMaximumPoolSize();
    }


}
