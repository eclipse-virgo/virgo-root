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

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.virgo.nano.core.Signal;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;


/**
 * {@link BlockingSignal} is a {@link Signal} that blocks until complete.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is thread safe.
 *
 */
public final class BlockingSignal implements Signal {

    private Object monitor = new Object();

    private final CountDownLatch latch = new CountDownLatch(1);

    private boolean complete = false;

    private Throwable cause = null;

    private final boolean block;

    public BlockingSignal(boolean block) {
        this.block = block;
    }

    /**
     * {@inheritDoc}
     */
    public void signalFailure(Throwable cause) {
        synchronized (this.monitor) {
            this.complete = true;
            this.cause = cause;
            latch.countDown();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void signalSuccessfulCompletion() {
        synchronized (this.monitor) {
            this.complete = true;
            latch.countDown();
        }
    }

    public boolean awaitCompletion(long timeInSeconds) throws DeploymentException {
        if (this.block) {
            try {
                this.latch.await(timeInSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException i) {
                throw new DeploymentException("latch await interrupted", i);
            }
        }
        synchronized (this.monitor) {
            if (!complete) {
                return false;
            } else {
                if (this.cause == null) {
                    return true;
                } else {
                    try {
                        throw this.cause;
                    } catch (DeploymentException de) {
                        throw de;
                    } catch (Throwable t) {
                        throw new DeploymentException(t.getMessage(), t.getCause());
                    }
                }
            }
        }
    }
    
    public boolean checkComplete() throws DeploymentException {
        if (this.block) {
            try {
                this.latch.await();
            } catch (InterruptedException i) {
                throw new DeploymentException("latch await interrupted", i);
            }
        }
        synchronized (this.monitor) {
            if (!complete) {
                return false;
            } else {
                if (this.cause == null) {
                    return true;
                } else {
                    try {
                        throw this.cause;
                    } catch (DeploymentException de) {
                        throw de;
                    } catch (Throwable t) {
                        throw new DeploymentException(t.getMessage(), t.getCause());
                    }
                }
            }
        }
    }

}
