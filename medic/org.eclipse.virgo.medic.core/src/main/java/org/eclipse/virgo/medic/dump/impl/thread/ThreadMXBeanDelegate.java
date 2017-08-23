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

package org.eclipse.virgo.medic.dump.impl.thread;

import java.lang.management.ThreadInfo;

/**
 * A delegate for ThreadMXBean to hide the differences in the methods and capabilities of its Java 5 and Java 6
 * versions.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Implementation <strong>must</strong> be thread-safe.
 * 
 */
interface ThreadMXBeanDelegate {

    /**
     * Finds all threads in the JVM that are deadlocked
     * 
     * @return An array of thread ids of the deadlocked threads
     */
    long[] findDeadlockedThreads();

    /**
     * Returns an array of <code>ThreadInfo</code> instances, one for each thread identified in the supplied array of
     * thread ids.
     * 
     * @param threadIds The thread ids
     * @return The array of <code>ThreadInfo</code> instances.
     */
    public ThreadInfo[] getThreadInfo(long[] threadIds);

    /**
     * Returns an array of <code>ThreadInfo</code> instances, one for each live thread.
     * 
     * @return The array of <code>ThreadInfo</code> instances.
     */
    public ThreadInfo[] dumpAllThreads();
}

