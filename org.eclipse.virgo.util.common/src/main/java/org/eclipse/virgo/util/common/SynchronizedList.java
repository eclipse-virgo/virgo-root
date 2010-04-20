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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * {@link SynchronizedList} wraps a given {@link List} and protects access to the collection and any
 * iterators created from the collection by synchronizing on a given monitor.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe, but ConcurrentModificationException may still be thrown.
 * 
 * @param <T> the type of the list elements
 */
public class SynchronizedList<T> extends SynchronizedCollection<T> implements List<T> {
    
    final Object monitor;
    
    private final List<T> list;

    @SuppressWarnings("unchecked")
    public SynchronizedList(List<? extends T> list, Object monitor) {
        super(list, monitor);
        this.monitor = monitor;
        this.list = (List<T>) list;
    }

    /** 
     * {@inheritDoc}
     */
    public void add(int index, T element) {
        synchronized (this.monitor) {
            this.list.add(index, element);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public boolean addAll(int index, Collection<? extends T> c) {
        synchronized (this.monitor) {
            return this.list.addAll(index, c);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public T get(int index) {
        synchronized (this.monitor) {
            return this.list.get(index);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public int indexOf(Object o) {
        synchronized (this.monitor) {
            return this.list.indexOf(o);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public int lastIndexOf(Object o) {
        synchronized (this.monitor) {
            return this.list.lastIndexOf(o);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public ListIterator<T> listIterator() {
        synchronized (this.monitor) {
            return new SynchronizedListIterator<T>(this.list.listIterator(), this.monitor);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public ListIterator<T> listIterator(int index) {
        synchronized (this.monitor) {
            return new SynchronizedListIterator<T>(this.list.listIterator(index), this.monitor);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public T remove(int index) {
        synchronized (this.monitor) {
            return this.list.remove(index);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public T set(int index, T element) {
        synchronized (this.monitor) {
            return this.list.set(index, element);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public List<T> subList(int fromIndex, int toIndex) {
        synchronized (this.monitor) {
            return new SynchronizedList<T>(this.list.subList(fromIndex, toIndex), this.monitor);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    
}
