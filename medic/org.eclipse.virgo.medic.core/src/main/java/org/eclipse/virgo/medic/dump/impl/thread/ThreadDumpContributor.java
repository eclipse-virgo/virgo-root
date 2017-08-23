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

import java.io.PrintWriter;
import java.lang.management.ThreadInfo;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;


public class ThreadDumpContributor implements DumpContributor {

    private final ThreadMXBeanDelegate threadBeanDelegate;

    private final ThreadInfoWriter threadInfoWriter;

    public ThreadDumpContributor(ThreadMXBeanDelegate threadBeanDelegate, ThreadInfoWriter threadInfoPrinter) {
        this.threadBeanDelegate = threadBeanDelegate;
        this.threadInfoWriter = threadInfoPrinter;
    }

    public ThreadDumpContributor() {
        String javaSpecificationVersion = System.getProperty("java.specification.version");
        if ("1.5".equals(javaSpecificationVersion)) {
            this.threadBeanDelegate = new Java5ThreadMXBeanDelegate();
            this.threadInfoWriter = new Java5ThreadInfoWriter();
        } else {
            this.threadBeanDelegate = new StandardThreadMXBeanDelegate();
            this.threadInfoWriter = new StandardThreadInfoWriter();
        }
    }

    public String getName() {
        return "thread";
    }

    /**
     * {@inheritDoc}
     */
    public void contribute(Dump dump) throws DumpContributionFailedException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(dump.createFileWriter("thread.txt"));
            processDeadlocks(writer);
            processAllThreads(writer);
            writer.close();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void processDeadlocks(PrintWriter dump) {
        dump.println("Deadlocked Threads");
        dump.println("==================");
        long[] deadlockedThreadIds = this.threadBeanDelegate.findDeadlockedThreads();
        if (deadlockedThreadIds != null) {
            dumpThreads(dump, this.threadBeanDelegate.getThreadInfo(deadlockedThreadIds));
        }
    }

    private void processAllThreads(PrintWriter dump) {
        dump.println();
        dump.println("All Threads");
        dump.println("===========");
        dumpThreads(dump, this.threadBeanDelegate.dumpAllThreads());
    }

    private void dumpThreads(PrintWriter dump, ThreadInfo[] infos) {
        for (ThreadInfo info : infos) {
            dump.println();
            this.threadInfoWriter.write(info, dump);
        }
    }
}
