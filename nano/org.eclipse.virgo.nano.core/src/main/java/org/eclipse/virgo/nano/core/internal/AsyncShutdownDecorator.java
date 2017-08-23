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

package org.eclipse.virgo.nano.core.internal;

import org.eclipse.virgo.nano.core.Shutdown;

/**
 * Decorator for a {@link Shutdown} implementation that performs all operations asynchronously.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class AsyncShutdownDecorator implements Shutdown {

    private static final String THREAD_NAME = "kernel-shutdown";
    private final Shutdown delegate;

    public AsyncShutdownDecorator(Shutdown delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    public void immediateShutdown() {
        new Thread(new Runnable() {

            public void run() {
                AsyncShutdownDecorator.this.delegate.immediateShutdown();
            }

        }, THREAD_NAME).start();
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() {
        new Thread(new Runnable() {

            public void run() {
                AsyncShutdownDecorator.this.delegate.shutdown();
            }

        }, THREAD_NAME).start();

    }

}
