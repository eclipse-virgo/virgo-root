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

/**
 * {@link SynchronizedObject} wraps a given {@link Object} and protects access to the object by synchronizing on a given
 * monitor.
 * <p />
 * Note that <code>hashCode</code> and <code>equals</code> are not delegated as this object is distinct from the wrapped object.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class SynchronizedObject extends Object {

    private final Object monitor;

    private final Object object;

    public SynchronizedObject(Object object, Object monitor) {
        if (object == null) {
            throw new IllegalArgumentException("null object");
        }
        if (monitor == null) {
            throw new IllegalArgumentException("null monitor");
        }
        this.object = object;
        this.monitor = monitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        synchronized (this.monitor) {
            return this.object.toString();
        }
    }

}
