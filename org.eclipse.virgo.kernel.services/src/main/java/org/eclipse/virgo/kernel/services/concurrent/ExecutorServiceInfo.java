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

import javax.management.MXBean;

/**
 * <p>
 * Anything that wants to expose information through the service registry about executor services must implement this
 * interface.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be threadsafe.
 * 
 */
@MXBean
public interface ExecutorServiceInfo {

    /**
     * Gets the average execution time of all executed tasks in mili seconds.
     * 
     * @return the average execution time.
     */
    long getAverageExecutionTime();

    /**
     * Gets the total time spent executing tasks in mili seconds.
     * 
     * @return the total execution time.
     */
    long getExecutionTime();

    /**
     * Gets the number of active tasks within this executor.
     * 
     * @return the active task count.
     */
    int getActiveCount();

    /**
     * Gets the current total capacity of the pool.
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
     * Gets the type of the pool.
     * 
     * @return the pool type name.
     */
    String getTypeName();
}
