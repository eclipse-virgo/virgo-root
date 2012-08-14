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

package org.eclipse.virgo.nano.services.concurrent;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Analyses any deadlocks present in the VM and creates a description of the cycles.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public class DeadlockAnalyser {

    /**
     * Result when there are no deadlocks.
     */
    private static final Deadlock[] NULL_RESULT = new Deadlock[0];

    /**
     * The VM {@link ThreadMXBean}.
     */
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    /**
     * Identifies deadlocks in the currently running {@link Thread Threads}.
     * 
     * @return the deadlocks; never <code>null</code>.
     */
    public Deadlock[] findDeadlocks() {
        long[] deadlockedThreads = this.threadBean.findMonitorDeadlockedThreads();
        if (deadlockedThreads == null || deadlockedThreads.length == 0) {
            return NULL_RESULT;
        }

        Map<Long, ThreadInfo> threadInfoMap = createThreadInfoMap(deadlockedThreads);
        Set<LinkedHashSet<ThreadInfo>> cycles = calculateCycles(threadInfoMap);
        Set<LinkedHashSet<ThreadInfo>> chains = calculateCycleDeadlockChains(threadInfoMap, cycles);
        cycles.addAll(chains);
        return createDeadlockDescriptions(cycles);
    }

    /**
     * Creates {@link Deadlock} objects for each cycle in the supplied set.
     * 
     * @param cycles the cycles.
     * @return the <code>Deadlocks</code>.
     */
    private Deadlock[] createDeadlockDescriptions(Set<LinkedHashSet<ThreadInfo>> cycles) {
        Deadlock[] result = new Deadlock[cycles.size()];
        int count = 0;
        for (Set<ThreadInfo> cycle : cycles) {
            ThreadInfo[] asArray = cycle.toArray(new ThreadInfo[cycle.size()]);
            Deadlock d = new Deadlock(asArray);
            result[count++] = d;
        }
        return result;
    }

    /**
     * Calculates the cycles in the supplied {@link ThreadInfo} map. A cycle is represented as a {@link LinkedHashSet}
     * of <code>ThreadInfo</code> objects. The order of the items in the <code>LinkedHashSet</code> reflects cycle
     * order.
     * 
     * @param threadInfoMap the <code>ThreadInfo</code> map.
     * @return all the cycles.
     */
    private Set<LinkedHashSet<ThreadInfo>> calculateCycles(Map<Long, ThreadInfo> threadInfoMap) {
        Set<LinkedHashSet<ThreadInfo>> cycles = new HashSet<LinkedHashSet<ThreadInfo>>();
        for (Map.Entry<Long, ThreadInfo> entry : threadInfoMap.entrySet()) {
            LinkedHashSet<ThreadInfo> cycle = new LinkedHashSet<ThreadInfo>();

            ThreadInfo t = entry.getValue();
            while (!cycle.contains(t)) {
                cycle.add(t);
                t = threadInfoMap.get(t.getLockOwnerId());
            }
            if (!cycles.contains(cycle)) {
                cycles.add(cycle);
            }
        }

        return cycles;
    }

    /**
     * Calculates deadlock chains where the deadlock occurs from a chain of threads waiting on some lock that is part of
     * a deadlock cycle. A cycle is represented as a {@link LinkedHashSet} of <code>ThreadInfo</code> objects. The order
     * of the items in the <code>LinkedHashSet</code> reflects chain order.
     * 
     * @param threadInfoMap the <code>ThreadInfo</code> map.
     * @param cycles the known deadlock cycles.
     * @return the deadlock chains.
     */
    private Set<LinkedHashSet<ThreadInfo>> calculateCycleDeadlockChains(Map<Long, ThreadInfo> threadInfoMap, Set<LinkedHashSet<ThreadInfo>> cycles) {
        ThreadInfo[] allThreads = this.threadBean.getThreadInfo(this.threadBean.getAllThreadIds());
        Set<LinkedHashSet<ThreadInfo>> deadlockChain = new HashSet<LinkedHashSet<ThreadInfo>>();
        Set<Long> knownDeadlockedThreads = threadInfoMap.keySet();
        for (ThreadInfo threadInfo : allThreads) {
            State state = threadInfo.getThreadState();
            if (state == State.BLOCKED && !knownDeadlockedThreads.contains(threadInfo.getThreadId())) {
                for (LinkedHashSet<ThreadInfo> cycle : cycles) {
                    if (cycle.contains(threadInfoMap.get(threadInfo.getLockOwnerId()))) {
                        LinkedHashSet<ThreadInfo> chain = new LinkedHashSet<ThreadInfo>();
                        ThreadInfo node = threadInfo;
                        while (!chain.contains(node)) {
                            chain.add(node);
                            node = threadInfoMap.get(node.getLockOwnerId());
                        }
                        deadlockChain.add(chain);
                    }
                }
            }
        }
        return deadlockChain;
    }

    /**
     * Creates a mapping of <code>ThreadId +> ThreadInfo</code> for the deadlocked threads.
     * 
     * @param threadIds the deadlocked thread ids
     * @return the mapping.
     */
    private Map<Long, ThreadInfo> createThreadInfoMap(long[] threadIds) {
        ThreadInfo[] threadInfos = this.threadBean.getThreadInfo(threadIds);

        Map<Long, ThreadInfo> threadInfoMap = new HashMap<Long, ThreadInfo>();
        for (ThreadInfo threadInfo : threadInfos) {
            threadInfoMap.put(threadInfo.getThreadId(), threadInfo);
        }
        return threadInfoMap;
    }

    /**
     * Describes a deadlock of two or more threads.
     * 
     */
    public static final class Deadlock {

        private final ThreadInfo[] members;

        private final String description;

        private final Set<Long> memberIds;

        /**
         * Creates a new {@link Deadlock}.
         * 
         * @param members the members of the deadlock in cycle order.
         */
        private Deadlock(ThreadInfo[] members) {
            this.members = members;
            this.memberIds = new HashSet<Long>(members.length);
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < members.length; x++) {
                ThreadInfo ti = members[x];
                sb.append(ti.getThreadName());
                if (x < members.length) {
                    sb.append(" > ");
                }

                if (x == members.length - 1) {
                    sb.append(ti.getLockOwnerName());
                }
                this.memberIds.add(ti.getThreadId());
            }
            this.description = sb.toString();
        }

        /**
         * Gets the members of the deadlock in cycle order.
         * 
         * @return the members of the deadlock.
         */
        public ThreadInfo[] getMembers() {
            return this.members.clone();
        }

        @Override
        public String toString() {
            return this.description;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.memberIds.hashCode();
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Deadlock other = (Deadlock) obj;
            return other.memberIds.equals(this.memberIds);
        }

    }
}
