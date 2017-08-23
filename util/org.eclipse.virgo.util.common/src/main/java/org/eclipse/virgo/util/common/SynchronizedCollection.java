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
import java.util.Iterator;

/**
 * {@link SynchronizedCollection} wraps a given {@link Collection} and protects access to the collection and any
 * iterators created from the collection by synchronizing on a given monitor.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe, but ConcurrentModificationException may still be thrown.
 * 
 * @param <T> type of element in collection
 */
public class SynchronizedCollection<T> extends SynchronizedObject implements Collection<T> {

    private final Collection<T> collection;

    private final Object monitor;

    private static final Object tieMonitor = new Object();

    /**
     * Creates a {@link SynchronizedCollection} wrapping the given collection and synchronizing on the given monitor,
     * neither of which may be <code>null</code>.
     * 
     * @param collection the collection to be wrapped, which must not be <code>null</code>
     * @param monitor the monitor which will be used to synchronize access to the collection
     */
    @SuppressWarnings("unchecked")
    public SynchronizedCollection(Collection<? extends T> collection, Object monitor) {
        super(collection, monitor); // throws an exception if either argument is null
        this.collection = (Collection<T>) collection;
        this.monitor = monitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        synchronized (this.monitor) {
            result = prime * result + collection.hashCode();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SynchronizedCollection<T> other = (SynchronizedCollection<T>) obj;
        int thisHash = System.identityHashCode(this);
        int otherHash = System.identityHashCode(other);
        if (thisHash < otherHash) {
            synchronized (this.monitor) {
                synchronized (other.monitor) {
                    if (!collection.equals(other.collection)) {
                        return false;
                    }
                }
            }
        } else if (thisHash > otherHash) {
            synchronized (other.monitor) {
                synchronized (this.monitor) {
                    if (!collection.equals(other.collection)) {
                        return false;
                    }
                }
            }
        } else {
            synchronized (tieMonitor) {
                synchronized (this.monitor) {
                    synchronized (other.monitor) {
                        if (!collection.equals(other.collection)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean add(T e) {
        synchronized (this.monitor) {
            return this.collection.add(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean addAll(Collection<? extends T> c) {
        synchronized (this.monitor) {
            return this.collection.addAll(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        synchronized (this.monitor) {
            this.collection.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(Object o) {
        synchronized (this.monitor) {
            return this.collection.contains(o);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsAll(Collection<?> c) {
        synchronized (this.monitor) {
            return this.collection.containsAll(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        synchronized (this.monitor) {
            return this.collection.isEmpty();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<T> iterator() {
        synchronized (this.monitor) {
            return new SynchronizedIterator<T>(this.collection.iterator(), this.monitor);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean remove(Object o) {
        synchronized (this.monitor) {
            return this.collection.remove(o);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeAll(Collection<?> c) {
        synchronized (this.monitor) {
            return this.collection.removeAll(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean retainAll(Collection<?> c) {
        synchronized (this.monitor) {
            return this.collection.retainAll(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        synchronized (this.monitor) {
            return this.collection.size();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object[] toArray() {
        synchronized (this.monitor) {
            return this.collection.toArray();
        }
    }

    /**
     * {@inheritDoc}
     */
    public <U> U[] toArray(U[] a) {
        synchronized (this.monitor) {
            return this.collection.toArray(a);
        }
    }

}
