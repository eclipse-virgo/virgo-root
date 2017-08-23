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

/**
 * Extension to {@link ExecutorService} that exposes useful metrics. <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> provide for threadsafe access to metrics. However, the timeliness of metric
 * updates need not be guaranteed.
 * 
 */
public interface ExecutorServiceStatistics {

    /**
     * Gets the average execution time of all executed tasks.
     * 
     * @return the average execution time.
     */
    long getAverageExecutionTime();

    /**
     * Gets the total time spend executing tasks.
     * 
     * @return the total execution time.
     */
    long getExecutionTime();

    /**
     * Gets the number of active tasks.
     * 
     * @return the active task count.
     */
    int getActiveCount();

    /**
     * Gets current size of the pool.
     * 
     * @return the current pool size.
     */
    int getPoolSize();

    /**
     * Gets the largest size the pool has ever reached.
     * 
     * @return the largest pool size.
     */
    int getLargestPoolSize();

    /**
     * Gets the maximum pool size.
     * 
     * @return the maximum pool size.
     */
    int getMaximumPoolSize();

    /**
     * Gets the name of the pool.
     * 
     * @return the pool name.
     */
    String getPoolName();

}
