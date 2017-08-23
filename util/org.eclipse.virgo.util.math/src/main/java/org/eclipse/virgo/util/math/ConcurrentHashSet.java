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

package org.eclipse.virgo.util.math;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConcurrentHashSet<Elem> implements ConcurrentSet<Elem> {

    private final ConcurrentMap<Elem, Boolean> elems = new ConcurrentHashMap<Elem, Boolean>();

    public boolean add(Elem e) {
        return null == this.elems.putIfAbsent(e, Boolean.TRUE);
    }

    public boolean addAll(Collection<? extends Elem> c) {
        boolean updated = false;
        for (Elem e : c) {
            updated = updated | add(e);
        }
        return updated;
    }

    public void clear() {
        this.elems.clear();
    }

    public boolean contains(Object o) {
        return this.elems.containsKey(o);
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return this.elems.isEmpty();
    }

    public Iterator<Elem> iterator() {
        return this.elems.keySet().iterator();
    }

    public boolean remove(Object o) {
        return null != this.elems.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        boolean updated = false;
        for (Object o : c) {
            updated = updated | remove(o);
        }
        return updated;
    }

    public boolean retainAll(Collection<?> c) {
        boolean updated = false;
        for (Elem e : this.elems.keySet()) {
            if (!c.contains(e)) {
                updated = true;
                this.elems.remove(e);
            }
        }
        return updated;
    }

    public int size() {
        return this.elems.size();
    }

    public Object[] toArray() {
        return this.elems.keySet().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return this.elems.keySet().toArray(a);
    }

}
