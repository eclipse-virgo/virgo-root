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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConcurrentMapRelation<X, Y> implements ConcurrentRelation<X, Y> {

    /*
     * Model a relation of type X <-> Y as a map to a power set X +> P Y. For correct concurrent behaviour the domain of
     * the map is monotonically increasing, i.e. we never remove an item even if the corresponding power set becomes
     * empty. For this reason, this type of relation should not be used for long lived relations with highly variable
     * domains.
     */
    private final ConcurrentMap<X, ConcurrentSet<Y>> rel = new ConcurrentHashMap<X, ConcurrentSet<Y>>();

    public boolean add(X x, Y y) {
        ensure(x);
        return this.rel.get(x).add(y);
    }

    public boolean contains(X x, Y y) {
        ensure(x);
        return this.rel.get(x).contains(y);
    }

    public Set<X> dom() {
        return this.rel.keySet();
    }

    /**
     * The result is potentially blurred in the presence of concurrency.
     */
    public Set<Y> ran() {
        Set<Y> range = new ConcurrentHashSet<Y>();
        for (ConcurrentSet<Y> rangeSet : this.rel.values()) {
            range.addAll(rangeSet);
        }
        return range;
    }

    public boolean remove(X x, Y y) {
        ensure(x);
        return this.rel.get(x).remove(y);
    }

    private void ensure(X x) {
        if (!this.rel.containsKey(x)) {
            this.rel.putIfAbsent(x, new ConcurrentHashSet<Y>());
        }
    }

    public Set<Y> relationalImage(Set<X> xset) {
        Set<Y> rImg = new ConcurrentHashSet<Y>();
        for (X x : xset) {
            if (this.rel.containsKey(x)) {
                rImg.addAll(this.rel.get(x));
            }
        }
        return rImg;
    }

    public void domSubtract(Set<X> xset) {
        for (X x : xset) {
            this.rel.remove(x);
        }
    }

}
