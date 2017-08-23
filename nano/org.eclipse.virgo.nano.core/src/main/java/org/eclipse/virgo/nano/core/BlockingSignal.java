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

package org.eclipse.virgo.nano.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A <code>Signal</code> implementation that blocks until notified of completion.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public final class BlockingSignal implements Signal {
    
    private final CountDownLatch latch = new CountDownLatch(1);
    
    private final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();

    /** 
     * {@inheritDoc}
     */
    public void signalFailure(Throwable cause) {
        this.failure.set(cause);
        this.latch.countDown();
    }

    /** 
     * {@inheritDoc}
     */
    public void signalSuccessfulCompletion() {
        this.latch.countDown();
    }
    
    public boolean awaitCompletion(long period, TimeUnit timeUnit) throws FailureSignalledException {
        try {
            if (!latch.await(period, timeUnit)) {
                return false;
            } else {
                Throwable failure = this.failure.get();
                if (failure != null) {
                    throw new FailureSignalledException(failure);
                } else {
                    return true;
                }
            }
        } catch (InterruptedException e) {
            throw new FailureSignalledException(e);
        }
    }
}
