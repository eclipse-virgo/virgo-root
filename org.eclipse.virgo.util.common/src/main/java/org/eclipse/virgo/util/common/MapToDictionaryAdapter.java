/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.util.common;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

/**
 * {@link MapToDictionaryAdapter} adapts a {@link Map} instance to conform to the {@link Dictionary} interface.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
public class MapToDictionaryAdapter<K, V> extends Dictionary<K, V> {
    
    private final Map<K, V> map;

    public MapToDictionaryAdapter(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Enumeration<K> keys() {
        return new IteratorToEnumerationAdapter<K>(map.keySet().iterator());
    }

    @Override
    public Enumeration<V> elements() {
        return new IteratorToEnumerationAdapter<V>(map.values().iterator());
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return this.map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.map.remove(key);
    }

}
