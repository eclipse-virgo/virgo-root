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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Test;

/**
 */
public abstract class AbstractExecutorTests {

    /*
     * Use Futures here so we can get the AssertionErrors back in the JUnit thread.
     */

    @Test public void testManuallyNamed() throws Throwable {
        submitAndAssert(getNamed("TestPool"), new Runnable() {

            public void run() {
                assertTrue(Thread.currentThread().getName().startsWith("TestPool"));
            }

        });
    }

    @Test public void testThreadLocalCleared() throws Throwable {

        final ThreadLocal<String> t = new ThreadLocal<String>();
        this.getExecutor().execute(new Runnable() {

            public void run() {
                t.set("foo");
            }

        });

        submitAndAssert(this.getExecutor(), new Runnable() {

            public void run() {
                assertNull("ThreadLocal should be null", t.get());
            }

        });

    }

    private void submitAndAssert(ExecutorService executor, Runnable task) throws Throwable {
        Future<?> f = executor.submit(task);
        assertFuture(f);
    }

    private void assertFuture(Future<?> future) throws Throwable {
        try {
            future.get();
        } catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }

    protected abstract ExecutorService getExecutor();

    protected abstract ExecutorService getNamed(String name);
}
