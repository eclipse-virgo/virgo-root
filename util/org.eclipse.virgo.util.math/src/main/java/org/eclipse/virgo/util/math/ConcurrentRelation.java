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

/**
 * Provide a mathematical relation which is notionally a set of ordered pairs. It represents a relationship between the
 * members of two sets: the domain and the range.
 * 
 * 
 * @param <X> the type of elements in the domain of the relation
 * @param <Y> the type of elements in the range of the relation
 */
public interface ConcurrentRelation<X, Y> {

    /**
     * Add the given pair (x, y) to the relation. It is not an error if the pair is already present.
     * 
     * @param x
     * @param y
     * @return true if and only if the relation was updated
     */
    boolean add(X x, Y y);

    /**
     * Remove the given pair (x, y) from the relation. It is not an error if the pair is not present.
     * 
     * @param x
     * @param y
     * @return true if and only if the relation was updated
     */
    boolean remove(X x, Y y);

    /**
     * Return true if and only if the given pair (x, y) is present in the relation.
     * 
     * @param x
     * @param y
     * @return true if the pair is present in the relation
     */
    boolean contains(X x, Y y);

    /**
     * Return the domain of the relation.
     * 
     * @return a set comprising the domain
     */
    Set<X> dom();

    /**
     * Return the range of the relation.
     * 
     * @return a set comprising the range
     */
    Set<Y> ran();

    /**
     * Return the relational image of a set of X's.
     * 
     * @param xset a set of X's
     * @return the set of Y's which relate to X's in xset
     */
    Set<Y> relationalImage(Set<X> xset);

    /**
     * Remove all the pairs (x, y) where x is in the given set of X's.
     * 
     * @param xset a set of X's
     */
    void domSubtract(Set<X> xset);
}
