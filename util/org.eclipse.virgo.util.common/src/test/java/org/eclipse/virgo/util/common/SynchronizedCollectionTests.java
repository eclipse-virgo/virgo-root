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
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.virgo.util.common.SynchronizedCollection;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class SynchronizedCollectionTests {
    
    private static final String S1 = "1";
    private static final String S2 = "2";

    private final Collection<String> c = new HashSet<String>();
    
    private final Object monitor = new Object();
    
    private SynchronizedCollection<String> sc;
    
    @Before
    public void setUp() {
        this.c.add(S1);
        this.sc = new SynchronizedCollection<String>(this.c, this.monitor);
    }
    
    @Test
    public void testToString() {
        assertEquals("[1]", this.sc.toString());
    }

    @Test
    public void testAdd() {
        assertTrue(this.sc.add(S2));
        assertFalse(this.sc.add(S2));
    }
    
    @Test
    public void testAddAll() {
        Collection<String> a = new HashSet<String>();
        a.add(S2);
        assertTrue(this.sc.addAll(a));
        assertFalse(this.sc.addAll(a));
    }
    
    @Test
    public void testClear() {
        assertFalse(this.sc.isEmpty());
        this.sc.clear();
        assertTrue(this.sc.isEmpty());
    }
    
    @Test
    public void testContains() {
        assertTrue(this.sc.contains(S1));
        assertFalse(this.sc.contains(S2));
    }
    
    @Test
    public void testContainsAll() {
        Collection<String> a = new HashSet<String>();
        a.add(S1);
        assertTrue(this.sc.containsAll(a));
        a.add(S2);
        assertFalse(this.sc.containsAll(a));
    }
    
    @Test
    public void testIsEmpty() {
        assertFalse(this.sc.isEmpty());
    }
    
    @Test
    public void testIterator() {
        Iterator<String> i = this.sc.iterator();
        assertEquals(S1, i.next());
    }
    
    @Test
    public void testRemove() {
        assertTrue(this.sc.contains(S1));
        assertTrue(this.sc.remove(S1));
        assertFalse(this.sc.contains(S1));
        assertFalse(this.sc.remove(S1));
    }
    
    @Test
    public void testRemoveAll() {
        Collection<String> a = new HashSet<String>();
        a.add(S1);
        a.add(S2);
        assertTrue(this.sc.removeAll(a));
        assertTrue(this.sc.isEmpty());
        assertFalse(this.sc.removeAll(a));
    }
    
    @Test
    public void testRetainAll() {
        Collection<String> a = new HashSet<String>();
        a.add(S1);
        assertFalse(this.sc.retainAll(a));
        assertFalse(this.sc.isEmpty());
        a.remove(S1);
        assertTrue(this.sc.retainAll(a));
        assertTrue(this.sc.isEmpty());
    }
    
    @Test
    public void testSize() {
        assertEquals(1, this.sc.size());
    }
    
    @Test
    public void testToObjectArray() {
        assertEquals(S1, this.sc.toArray()[0]);
    }
    
    @Test
    public void testToTypesArray() {
        assertEquals(S1, this.sc.toArray(new String[0])[0]);
    }
    
    @Test
    public void testHashCodeEquals() {
        
        assertFalse(this.sc.equals(null));
        
        assertFalse(this.sc.equals(new Object()));
        
        this.sc.add(S2);
        
        Collection<String> s = new HashSet<String>();
        s.add(S2);
        s.add(S1);
        
        assertEquals(this.c, s);
        
        SynchronizedCollection<String> sc2 = new SynchronizedCollection<String>(s, this.monitor);
        
        assertEquals(this.sc.hashCode(), sc2.hashCode());
        assertTrue(this.sc.equals(this.sc));
        assertTrue(this.sc.equals(sc2));
        assertTrue(sc2.equals(this.sc));
        
        Collection<String> e = new HashSet<String>();
        SynchronizedCollection<String> empty = new SynchronizedCollection<String> (e, this.monitor);
        assertFalse(this.sc.equals(empty));
        assertFalse(empty.equals(this.sc));
    }
    
}
