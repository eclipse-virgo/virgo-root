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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.virgo.util.common.SynchronizedList;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class SynchronizedListTests {
    
    private static final String S1 = "1";
    private static final String S2 = "2";
    private static final String S3 = "2";

    private final List<String> l = new ArrayList<String>();
    
    private final Object monitor = new Object();
    
    private SynchronizedList<String> sl;
    
    @Before
    public void setUp() {
        this.l.add(S1);
        this.sl = new SynchronizedList<String>(this.l, this.monitor);
    }
    
    @Test
    public void testAddAtIndex() {
        assertEquals(S1, this.sl.get(0));
        this.sl.add(0,S2);
        assertEquals(S2, this.sl.get(0));
        assertEquals(S1, this.sl.get(1));
    }
    
    @Test
    public void testAddAllAtIndex() {
        Collection<String> a = new ArrayList<String>();
        a.add(S2);
        a.add(S3);
        assertTrue(this.sl.addAll(0,a));
        assertEquals(S2, this.sl.get(0));
        assertEquals(S3, this.sl.get(1));
        assertEquals(S1, this.sl.get(2));
    }
    
    @Test
    public void testGet() {
        assertEquals(S1, this.sl.get(0));
    }

    @Test
    public void testIndexOf() {
        assertEquals(0, this.sl.indexOf(S1));
    }
    
    @Test
    public void testLastIndexOf() {
        this.sl.add(S2);
        this.sl.add(S1);
        assertEquals(2, this.sl.lastIndexOf(S1));
    }
    
    @Test
    public void testListIterator() {
        ListIterator<String> li = this.sl.listIterator();
        assertEquals(S1, li.next());
    }
    
    @Test
    public void testListIteratorFromIndex() {
        this.sl.add(S2);
        ListIterator<String> li = this.sl.listIterator(1);
        assertEquals(S2, li.next());
    }
    
    @Test
    public void testSetAtIndex() {
        assertEquals(S1, this.sl.set(0,S2));
        assertEquals(S2, this.sl.get(0));
    }
    
    @Test
    public void testSublist() {
        this.sl.add(S2);
        this.sl.add(S3);
        List<String> subList = this.sl.subList(1, 2);
        assertEquals(1, subList.size());
        assertEquals(S2, subList.get(0));
    }
    
    @Test
    public void testRemoveFromIndex() {
        assertEquals(S1, this.sl.remove(0));
    }
    
    @Test
    public void testAdd() {
        assertTrue(this.sl.add(S2));
    }
    
    @Test
    public void testAddAll() {
        Collection<String> a = new HashSet<String>();
        a.add(S2);
        assertTrue(this.sl.addAll(a));
    }
    
    @Test
    public void testClear() {
        assertFalse(this.sl.isEmpty());
        this.sl.clear();
        assertTrue(this.sl.isEmpty());
    }
    
    @Test
    public void testContains() {
        assertTrue(this.sl.contains(S1));
        assertFalse(this.sl.contains(S2));
    }
    
    @Test
    public void testContainsAll() {
        Collection<String> a = new HashSet<String>();
        a.add(S1);
        assertTrue(this.sl.containsAll(a));
        a.add(S2);
        assertFalse(this.sl.containsAll(a));
    }
    
    @Test
    public void testIsEmpty() {
        assertFalse(this.sl.isEmpty());
    }
    
    @Test
    public void testIterator() {
        Iterator<String> i = this.sl.iterator();
        assertEquals(S1, i.next());
    }
    
    @Test
    public void testRemove() {
        assertTrue(this.sl.contains(S1));
        assertTrue(this.sl.remove(S1));
        assertFalse(this.sl.contains(S1));
        assertFalse(this.sl.remove(S1));
    }
    
    @Test
    public void testRemoveAll() {
        Collection<String> a = new HashSet<String>();
        a.add(S1);
        a.add(S2);
        assertTrue(this.sl.removeAll(a));
        assertTrue(this.sl.isEmpty());
        assertFalse(this.sl.removeAll(a));
    }
    
    @Test
    public void testRetainAll() {
        Collection<String> a = new HashSet<String>();
        a.add(S1);
        assertFalse(this.sl.retainAll(a));
        assertFalse(this.sl.isEmpty());
        a.remove(S1);
        assertTrue(this.sl.retainAll(a));
        assertTrue(this.sl.isEmpty());
    }
    
    @Test
    public void testSize() {
        assertEquals(1, this.sl.size());
    }
    
    @Test
    public void testToObjectArray() {
        assertEquals(S1, this.sl.toArray()[0]);
    }
    
    @Test
    public void testToTypesArray() {
        assertEquals(S1, this.sl.toArray(new String[0])[0]);
    }
    
    @Test
    public void testHashCodeEquals() {
        this.sl.add(S2);
        
        List<String> a = new ArrayList<String>();
        a.add(S1);
        a.add(S2);
        
        assertEquals(this.l, a);
        
        SynchronizedList<String> sl2 = new SynchronizedList<String>(a, this.monitor);
        
        assertEquals(this.sl.hashCode(), sl2.hashCode());
        assertTrue(this.sl.equals(this.sl));
        assertTrue(this.sl.equals(sl2));
    }
    
}
