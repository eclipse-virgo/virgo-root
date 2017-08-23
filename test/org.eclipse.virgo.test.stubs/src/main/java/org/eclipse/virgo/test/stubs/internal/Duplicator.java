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

package org.eclipse.virgo.test.stubs.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Utility methods for stub implementations
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public class Duplicator {

    /**
     * Shallow copy the contents of a {@link Dictionary} into a new instance
     * 
     * @param <K> The type of keys in the {@link Dictionary}
     * @param <V> The type of values in the {@link Dictionary}
     * 
     * @param in The {@link Dictionary} to copy
     * @return A new, shallow copied, instance of {@link Dictionary}
     */
    public static <K, V> Dictionary<K, V> shallowCopy(Dictionary<K, V> in) {
        Hashtable<K, V> out = new Hashtable<K, V>(in.size());

        Enumeration<K> keys = in.keys();
        while (keys.hasMoreElements()) {
            K key = keys.nextElement();
            V value = in.get(key);
            out.put(key, value);
        }

        return out;
    }

    /**
     * Shallow copy the contents of a {@link List} into a new instance
     * 
     * @param <T> The type of the values in the {@link List}
     * @param in The {@link List} to copy
     * @return A new, shallow copied, instance of the {@link List}
     */
    public static <T> List<T> shallowCopy(List<T> in) {
        return new ArrayList<T>(in);
    }
}
