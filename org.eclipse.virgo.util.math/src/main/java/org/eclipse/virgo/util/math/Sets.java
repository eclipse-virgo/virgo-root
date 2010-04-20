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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines common operations on {@link Set Sets}.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public class Sets {

    /**
     * Calculates the difference between <code>a</code> and <code>b</code>.
     * 
     * @param <T> the type of the <code>Set</code>.
     * @param a the lhs of the difference.
     * @param b the rhs of the difference.
     * @return the difference between <code>a</code> and <code>b</code>.
     */
	public static <T> Set<T> difference(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.removeAll(b);
        return result;
    }

    /**
     * Calculates the intersection between <code>a</code> and <code>b</code>.
     * 
     * @param <T> the type of the <code>Set</code>.
     * @param a the lhs of the intersection.
     * @param b the rhs of the intersection.
     * @return the intersection between <code>a</code> and <code>b</code>.
     */
    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> iter = a.size() < b.size() ? a : b;
        Set<T> other = iter == a ? b : a;

        Set<T> result = new HashSet<T>();
        for (T t : iter) {
            if (other.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Creates a <code>Set</code> containing the supplied items.
     * 
     * @param <T> the type of the <code>Set</code> entries.
     * @param items the <code>Set</code> contents.
     * @return the resultant <code>Set</code>.
     */
    public static <T> Set<T> asSet(T... items) {
        Set<T> set = new HashSet<T>(items.length);
        Collections.addAll(set, items);
        return set;
    }
}
