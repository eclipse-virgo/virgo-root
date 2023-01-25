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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.virgo.util.common.CaseInsensitiveMap;
import org.junit.Test;

/**
 */
public class CaseInsensitiveMapTests {
    
    @Test
    public void testClear() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("oNe", 1);
        assertFalse(m.equals(new CaseInsensitiveMap<Integer>()));
        m.clear();
        assertEquals(new CaseInsensitiveMap<Integer>(), m);
        assertTrue(m.isEmpty());
    }
    
    @Test
    public void testGet() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("oNe", 1);
        assertEquals(Integer.valueOf(1), m.get("oNe"));
        assertEquals(null, m.get("two"));
        assertEquals(null, m.get(null));
    }

    @Test
    public void testContainsKey() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("oNe", 1);
        assertTrue(m.containsKey("oNe"));
        assertTrue(m.containsKey("one"));
        assertTrue(m.containsKey("ONe"));
        assertTrue(m.containsKey("ONE"));
        assertFalse(m.containsKey(m));
        assertFalse(m.containsKey(null));
    }

    @Test
    public void testContainsValue() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("oNe", 1);
        assertTrue(m.containsValue(1));
    }

    @Test
    public void testEntrySet() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("oNe", 1);
        m.put("Two", 2);
        Set<Entry<String, Integer>> e = m.entrySet();
        assertTrue(e.contains(new Entry<String, Integer>() {

            public String getKey() {
                return "one";
            }

            public Integer getValue() {
                return 1;
            }

            public Integer setValue(Integer value) {
                return null;
            }
        }));
        assertFalse(e.contains(new Entry<String, Integer>() {

            public String getKey() {
                return "one";
            }

            public Integer getValue() {
                return null;
            }

            public Integer setValue(Integer value) {
                return null;
            }
        }));
        assertEquals(2, e.size());
        for (Entry<String, Integer> entry : e) {
            if ("Two".equals(entry.getKey())) {
                assertEquals(Integer.valueOf(2), entry.getValue());
                entry.setValue(3);
            }
        }
        assertEquals(Integer.valueOf(3), m.get("tWO"));
        assertTrue(m.containsKey("one"));
        for (Entry<String, Integer> entry : e) {
            if ("oNe".equals(entry.getKey())) {
                assertTrue(e.remove(entry));
            }
        }
        assertFalse(m.containsKey("one"));
        assertFalse(e.contains(e));
        m.put("thrEE", 3);
        m.put("foUr", 4);
        assertEquals(3, m.size());
        Iterator<Entry<String, Integer>> iterator = e.iterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        iterator.remove();
        assertEquals(2, m.size());
        e.clear();
        assertFalse(e.contains(e));
    }

    @Test(expected = NullPointerException.class)
    public void testNullKey() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put(null, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testNullValue() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("nuLL", null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetAdd() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.entrySet().add(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetAddAll() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.entrySet().addAll(null);
    }

    @Test
    public void testKeySet() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("oNe", 1);
        m.put("Two", 2);
        Set<String> k = m.keySet();
        assertTrue(k.contains("oNe"));
        assertTrue(k.contains("one"));
        assertFalse(k.contains(k));
        assertFalse(m.containsKey(m));
        assertEquals(2, k.size());
        Iterator<String> i = k.iterator();
        while (i.hasNext()) {
            if ("oNe".equals(i.next())) {
                i.remove();
            }
        }
        assertTrue(m.containsKey("two"));
        k.remove("TWO");
        assertFalse(m.containsKey("two"));
        m.put("three", 3);
        k.clear();
        assertTrue(m.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetAdd() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.keySet().add(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetAddAll() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.keySet().addAll(null);
    }

    @Test
    public void testRemove() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("oNe", 1);
        m.put("Two", 2);
        assertEquals(2, m.size());
        m.remove("one");
        assertFalse(m.containsKey("oNe"));
        assertEquals(1, m.size());
        m.remove(null);
    }

    @Test
    public void testValues() {
        CaseInsensitiveMap<Integer> m = new CaseInsensitiveMap<Integer>();
        m.put("oNe", 1);
        m.put("Two", 2);
        Collection<Integer> v = m.values();
        assertTrue(v.contains(1));
        assertTrue(v.contains(2));
        assertEquals(2, v.size());
    }

    @Test
    public void testCaseInsensitiveKey() {
        CaseInsensitiveMap.CaseInsensitiveKey k = CaseInsensitiveMap.CaseInsensitiveKey.objectToKey("onE");
        assertTrue(k.equals(CaseInsensitiveMap.CaseInsensitiveKey.objectToKey("ONE")));
        assertFalse(k.equals(CaseInsensitiveMap.CaseInsensitiveKey.objectToKey(null)));
        assertFalse(k.equals(CaseInsensitiveMap.CaseInsensitiveKey.objectToKey("TWO")));
        assertFalse(k.equals(null));
        assertFalse(k.equals(this));
        assertFalse((CaseInsensitiveMap.CaseInsensitiveKey.objectToKey(null)).equals(k));
        assertFalse((CaseInsensitiveMap.CaseInsensitiveKey.objectToKey(null)).equals(null));
        assertTrue((CaseInsensitiveMap.CaseInsensitiveKey.objectToKey(null)).equals(CaseInsensitiveMap.CaseInsensitiveKey.objectToKey(null)));
    }
    
    @Test
    public void testConstructorFromMap() {
        CaseInsensitiveMap<String> m = new CaseInsensitiveMap<String>();
        m.put("a", "a");
        CaseInsensitiveMap<String> n = new CaseInsensitiveMap<String>(m);
        assertEquals(m, n);
    }
    
}

