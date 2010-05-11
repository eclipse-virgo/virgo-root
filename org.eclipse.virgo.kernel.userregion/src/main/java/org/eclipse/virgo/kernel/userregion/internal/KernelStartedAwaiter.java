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

package org.eclipse.virgo.kernel.userregion.internal;

import java.util.concurrent.CountDownLatch;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;


/**
 * A helper class that awaits the <code>EventAdmin<code> event for the
 * kernel having started.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class KernelStartedAwaiter implements EventHandler {
    
    private static final String TOPIC_KERNEL_STARTED = "org/eclipse/virgo/kernel/STARTED";
    
    private final CountDownLatch latch = new CountDownLatch(1);

    /** 
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
        if (TOPIC_KERNEL_STARTED.equals(event.getTopic())) {
            this.latch.countDown();
        }
    }
    
    void awaitKernelStarted() throws InterruptedException {
        this.latch.await();
    }
}
