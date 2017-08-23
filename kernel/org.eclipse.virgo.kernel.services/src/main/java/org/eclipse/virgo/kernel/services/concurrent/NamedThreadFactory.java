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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ThreadFactory} implementation that delegates to the {@link ThreadManager}. Automatic naming of threads in the
 * pool is handled by this class.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class NamedThreadFactory implements ThreadFactory {

    private final String poolName;

    private final AtomicInteger threadCount = new AtomicInteger(1);

    /**
     * Creates a new <code>ThreadManagerPoolThreadFactory</code>.
     * 
     * @param poolName the name of the thread pool.
     */
    public NamedThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    /**
     * {@inheritDoc}
     */
    public Thread newThread(Runnable r) {
        return new Thread(r, this.poolName + "-thread-" + this.threadCount.getAndIncrement());
    }
}
