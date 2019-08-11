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

package org.eclipse.virgo.kernel.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.virgo.nano.core.AbortableSignal;

public final class TestSignal implements AbortableSignal {
    
    private final AtomicReference<Throwable> causeReference = new AtomicReference<>(null);
    
    private final CountDownLatch completionLatch = new CountDownLatch(1);

    /** 
     * {@inheritDoc}
     */
    public void signalFailure(Throwable cause) {
        causeReference.set(cause);
        completionLatch.countDown();
    }

    /** 
     * {@inheritDoc}
     */
    public void signalSuccessfulCompletion() {
        completionLatch.countDown();
    }

	public void signalAborted() {
        completionLatch.countDown();
	}  
    
    public void assertSuccessfulCompletionSignalled(long msTimeout) throws InterruptedException {
        assertTrue(completionLatch.await(msTimeout, TimeUnit.MILLISECONDS));
        assertNull(causeReference.get());
    }
    
    public void assertFailureSignalled(long msTimeout) throws InterruptedException {
        assertTrue(completionLatch.await(msTimeout, TimeUnit.MILLISECONDS));
        assertNotNull(causeReference.get());
    }

	public void assertsignalAborted(long msTimeout) throws InterruptedException {
        assertTrue(completionLatch.await(msTimeout, TimeUnit.MILLISECONDS));
        assertNull(causeReference.get());
	}      
}
