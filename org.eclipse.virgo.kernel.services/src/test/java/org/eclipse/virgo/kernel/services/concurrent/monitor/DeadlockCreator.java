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

package org.eclipse.virgo.kernel.services.concurrent.monitor;

import java.lang.Thread.State;
import java.util.concurrent.CountDownLatch;

/**
 */
public class DeadlockCreator implements DeadlockCreatorMBean {

    public Thread[] createDeadlock(int threadCount, int extraneousCount) {
        CountDownLatch latch = new CountDownLatch(threadCount);
        Object[] monitors = new Object[threadCount];

        for (int x = 0; x < threadCount; x++) {
            monitors[x] = new Object();
        }

        Thread[] threads = new Thread[threadCount];
        for (int x = 0; x < threadCount; x++) {
            int f = x;
            int s = x == threadCount - 1 ? 0 : x + 1;
            threads[x] = new Thread(new DeadlockRunnable(latch, monitors[f], monitors[s]));
            threads[x].start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        awaitBlocking(threads);

        Thread[] extraneous = new Thread[extraneousCount];
        for (int x = 0; x < extraneousCount; x++) {
            extraneous[x] = new Thread(new ExtraneousRunnable(monitors[0]));
            extraneous[x].start();
        }
        awaitBlocking(extraneous);
        return threads;
    }

    void awaitBlocking(Thread[] threads) {
        for (Thread thread : threads) {
            while (thread.getState() != State.BLOCKED) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static final class DeadlockRunnable implements Runnable {

        private final CountDownLatch latch;

        private final Object first;

        private final Object second;

        /**
         * @param latch
         * @param first
         * @param second
         */
        public DeadlockRunnable(CountDownLatch latch, Object first, Object second) {
            this.latch = latch;
            this.first = first;
            this.second = second;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            synchronized (this.first) {
                this.latch.countDown();
                try {
                    this.latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (this.second) {

                }
            }
        }

    }

    static final class ExtraneousRunnable implements Runnable {

        private final Object monitor;

        /**
         * @param monitor
         */
        public ExtraneousRunnable(Object monitor) {
            this.monitor = monitor;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            synchronized (this.monitor) {

            }
        }

    }

}
