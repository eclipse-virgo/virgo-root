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

import java.util.ListIterator;

/**
 * {@link SynchronizedListIterator} wraps a given {@link ListIterator} and protects access to the iterator by
 * synchronizing on the monitor of the containing {@link SynchronizedList}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe, but ConcurrentModificationException may still be thrown.
 * 
 * @param <T> the type of the list elements
 */
public class SynchronizedListIterator<T> extends SynchronizedIterator<T> implements ListIterator<T> {

    private final Object monitor;

    private final ListIterator<T> listIterator;

    public SynchronizedListIterator(ListIterator<T> listIterator, Object monitor) {
        super(listIterator, monitor);
        this.monitor = monitor;
        this.listIterator = listIterator;
    }

    /**
     * {@inheritDoc}
     */
    public void add(T e) {
        synchronized (this.monitor) {
            this.listIterator.add(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPrevious() {
        synchronized (this.monitor) {
            return this.listIterator.hasPrevious();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int nextIndex() {
        synchronized (this.monitor) {
            return this.listIterator.nextIndex();
        }
    }

    /**
     * {@inheritDoc}
     */
    public T previous() {
        synchronized (this.monitor) {
            return this.listIterator.previous();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int previousIndex() {
        synchronized (this.monitor) {
            return this.listIterator.previousIndex();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void set(T e) {
        synchronized (this.monitor) {
            this.listIterator.set(e);
        }
    }

}
