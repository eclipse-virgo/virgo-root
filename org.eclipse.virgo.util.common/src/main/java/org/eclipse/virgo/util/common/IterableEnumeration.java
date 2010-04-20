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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An <code>IterableEnumeration</code> wraps an {@link Enumeration} in an
 * implementation of {@link Iterable} thereby allowing the <code>Enumeration</code>
 * to be used in a for-each loop.
 * <p/>
 * 
 * <pre>
 * Enumeration<Foo> enumeration = ...
 * IterableEnumeration<Foo> foos = new IterableEnumeration<Foo>(enumeration);
 * 
 * for (Foo foo : foos) {
 *     // Do something with foo
 * }
 * </pre>
 * 
 * <p/>
 * <strong>Note:</strong> as <code>Enumerations</code> do not support entry removal
 * the {@link Iterator} returned by {@link #iterator()} does not support the optional
 * {@link Iterator#remove() remove} method: invocation of the method will result in an
 * {@link UnsupportedOperationException} being thrown.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * This class is <strong>not</strong> thread-safe.
 * @param <T> the element type of the enumeration
 * 
 */
public class IterableEnumeration<T> implements Iterable<T> {
    
    private final Iterator<T> iterator;
    
    /**
     * Creates a new <code>IterableEnumeration</code> backed by the supplied <code>Enumeration</code>
     * @param enumeration The <code>Enumeration</code> to be made iterable.
     */
    public IterableEnumeration(Enumeration<T> enumeration) {
        this.iterator = new EnumerationIterator<T>(enumeration);
    }

    /**
     * Returns the <code>Enumeration</code>-backed <code>Iterator</code>.
     * <p/>
     * <strong>Note:</strong> as <code>Enumerations</code> do not support entry removal
     * the returned {@link Iterator} does not support the optional
     * {@link Iterator#remove() remove} method: invocation of the method will result in an
     * {@link UnsupportedOperationException} being thrown.
     */
    public Iterator<T> iterator() {
        return iterator;
    }
    
    private class EnumerationIterator<E> implements Iterator<E> {
        
        private final Enumeration<E> enumeration;
        
        private EnumerationIterator(Enumeration<E> enumeration) {
            this.enumeration = enumeration;
        }

        public boolean hasNext() {
            return this.enumeration.hasMoreElements();
        }

        public E next() {
            return this.enumeration.nextElement();
        }

        public void remove() {
            throw new UnsupportedOperationException();
            
        }       
    }
}
