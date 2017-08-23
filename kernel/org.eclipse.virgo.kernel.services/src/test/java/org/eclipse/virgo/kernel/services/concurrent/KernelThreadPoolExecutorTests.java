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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.kernel.services.concurrent.KernelThreadPoolExecutor;
import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.junit.Test;

/**
 * Note that this class used to test the value of getCompletedTaskCount after an execution which failed with an
 * exception, but the implementation of ThreadPoolExecutor changed in Java 7 so that getCompletedTaskCount included
 * tasks that failed with an exception. So the test was removed.
 */
public class KernelThreadPoolExecutorTests extends AbstractExecutorTests {

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

    private final StubTracingService tracingService = new StubTracingService();

    /**
     * {@inheritDoc}
     */
    @Override
    protected KernelThreadPoolExecutor getExecutor() {
        return getNamed(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected KernelThreadPoolExecutor getNamed(String name) {
        return new KernelThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, this.queue, name, this.tracingService);
    }

    @Test
    public void statisticsOnSuccess() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        KernelThreadPoolExecutor executor = getExecutor();
        executor.execute(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }

        });
        latch.await();
        Thread.sleep(100);
        assertEquals(1, executor.getCompletedTaskCount());
        assertTrue(executor.getAverageExecutionTime() > 0);
        assertTrue(executor.getExecutionTime() > 0);
    }

    @Test
    public void traceNamePropagated() throws InterruptedException {
        this.tracingService.setCurrentApplicationName("foo");
        final CountDownLatch latch = new CountDownLatch(1);

        KernelThreadPoolExecutor executor = getExecutor();
        executor.execute(new Runnable() {

            public void run() {
                if ("foo".equals(tracingService.getCurrentApplicationName())) {
                    latch.countDown();
                }
            }

        });
        boolean result = latch.await(2, TimeUnit.SECONDS);
        assertTrue(result);
    }

    private static final class StubTracingService implements TracingService {

        private String applicationName;

        public String getCurrentApplicationName() {
            return applicationName;
        }

        public void setCurrentApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

    }
}
