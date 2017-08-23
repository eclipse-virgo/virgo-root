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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * A Java 5-based implementation of <code>ThreadMXBeanDelegate</code>. This class requires a Java 5 (or later) VM.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class Java5ThreadMXBeanDelegate implements ThreadMXBeanDelegate {

    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    /**
     * {@inheritDoc}
     */
    public long[] findDeadlockedThreads() {
        return threadBean.findMonitorDeadlockedThreads();
    }

    /**
     * {@inheritDoc}
     */
    public ThreadInfo[] getThreadInfo(long[] threadIds) {
        return threadBean.getThreadInfo(threadIds, Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    public ThreadInfo[] dumpAllThreads() {
        return threadBean.getThreadInfo(threadBean.getAllThreadIds(), Integer.MAX_VALUE);
    }
}

