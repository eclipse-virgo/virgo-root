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

package org.eclipse.virgo.util.osgi.manifest.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.virgo.util.osgi.manifest.internal.MapUpdatingList;
import org.junit.Test;


public class MapUpdatingListTests {
    
    private final Map<String, String> map = new HashMap<String, String>();
    
    private final String key = "key";
    
    private final List<String> list = new MapUpdatingList(this.map, this.key);
    
    @Test
    public void add() {        
        list.add("foo");
        assertEquals("foo", map.get(key));
        list.add("foo");
        assertEquals("foo,foo", map.get(key));
    }
    
    @Test
    public void addAtIndex() {        
        list.add("foo");
        assertEquals("foo", map.get(key));
        list.add(0, "bar");
        assertEquals("bar,foo", map.get(key));
    }
    
    @Test
    public void addAll() {
        list.addAll(Arrays.asList("a"));
        assertEquals("a", map.get(key));
        list.addAll(Arrays.asList("b", "c"));
        assertEquals("a,b,c", map.get(key));
    }
    
    @Test
    public void addAllAtIndex() {
        list.addAll(Arrays.asList("a"));
        assertEquals("a", map.get(key));
        list.addAll(0, Arrays.asList("b", "c"));
        assertEquals("b,c,a", map.get(key));
    }
    
    @Test
    public void clear() {
        list.add("a");
        list.add("b");
        assertEquals("a,b", map.get(key));
        list.clear();
        assertNull(map.get(key));
    }
    
    @Test
    public void removeAtIndex() {
        list.add("a");
        list.add("b");
        list.add("c");
        assertEquals("a,b,c", map.get(key));
        assertEquals("b", list.remove(1));
        assertEquals("a,c", map.get(key));
        assertEquals("a", list.remove(0));
        assertEquals("c", list.remove(0));
        assertNull(map.get(key));
    }
    
    @Test
    public void remove() {
        list.add("a");
        list.add("b");
        list.add("c");
        assertEquals("a,b,c", map.get(key));
        assertTrue(list.remove("b"));
        assertEquals("a,c", map.get(key));
        assertTrue(list.remove("a"));
        assertTrue(list.remove("c"));
        assertNull(map.get(key));
        assertFalse(list.remove("a"));
    }
    
    @Test
    public void removeAll() {
        list.add("a");
        list.add("b");
        list.add("c");
        assertEquals("a,b,c", map.get(key));
        assertTrue(list.removeAll(Arrays.asList("b")));
        assertEquals("a,c", map.get(key));
        assertTrue(list.removeAll(Arrays.asList("a", "c", "d")));
        assertNull(map.get(key));
        assertFalse(list.removeAll(Arrays.asList("a", "b")));
    }
    
    @Test
    public void retainAll() {
        list.add("a");
        list.add("b");
        list.add("a");
        list.add("c");
        assertEquals("a,b,a,c", map.get(key));
        assertTrue(list.retainAll(Arrays.asList("a", "b")));
        assertEquals("a,b,a", map.get(key));
        assertFalse(list.retainAll(Arrays.asList("a", "b")));
        assertTrue(list.retainAll(Arrays.asList("c")));
        assertNull(map.get(key));
    }
    
    @Test
    public void set() {
        list.add("a");
        list.add("b");
        list.add("c");
        assertEquals("a,b,c", map.get(key));
        assertEquals("b", list.set(1, "d"));
        assertEquals("a,d,c", map.get(key));
    }
}
