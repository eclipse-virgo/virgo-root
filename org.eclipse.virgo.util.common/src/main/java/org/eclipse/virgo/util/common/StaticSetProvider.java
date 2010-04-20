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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link SetProvider} that takes a static list of elements and returns them for every request for
 * a set.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 * @param <T> type of elements of set
 */
public class StaticSetProvider<T> implements SetProvider<T> {

    private final Set<T> set;

    public StaticSetProvider(T... items) {
        Set<T> set = new HashSet<T>(items.length);
        Collections.addAll(set, items);
        this.set = Collections.unmodifiableSet(set);
    }

    public Set<T> getSet() {
        return set;
    }

}
