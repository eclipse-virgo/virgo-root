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

public class StandardThreadMXBeanDelegate implements ThreadMXBeanDelegate {

    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    public ThreadInfo[] dumpAllThreads() {
        return threadBean.dumpAllThreads(true, true);
    }

    public long[] findDeadlockedThreads() {
        return threadBean.findDeadlockedThreads();
    }

    public ThreadInfo[] getThreadInfo(long[] threadIds) {
        return threadBean.getThreadInfo(threadIds, true, true);
    }

}
