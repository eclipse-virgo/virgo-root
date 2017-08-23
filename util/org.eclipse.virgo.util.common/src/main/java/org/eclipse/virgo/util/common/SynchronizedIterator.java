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

package org.eclipse.virgo.util.common;

import java.util.Iterator;

/**
 * {@link SynchronizedIterator} wraps a given {@link Iterator} and protects access to the iterator by synchronizing on
 * the monitor of the containing {@link SynchronizedCollection}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe, but ConcurrentModificationException may still be thrown.
 * 
 * @param <T> the type of element the iterator returns
 */
public class SynchronizedIterator<T> extends SynchronizedObject implements Iterator<T> {

    private final Object monitor;

    private final Iterator<T> iterator;

    public SynchronizedIterator(Iterator<T> iterator, Object monitor) {
        super(iterator, monitor);
        this.monitor = monitor;
        this.iterator = iterator;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        synchronized (this.monitor) {
            return this.iterator.hasNext();
        }
    }

    /**
     * {@inheritDoc}
     */
    public T next() {
        synchronized (this.monitor) {
            return this.iterator.next();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        synchronized (this.monitor) {
            this.iterator.remove();
        }
    }

}
