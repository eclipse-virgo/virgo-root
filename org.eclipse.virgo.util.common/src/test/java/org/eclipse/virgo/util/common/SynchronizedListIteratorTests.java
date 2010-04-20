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
import java.util.List;
import java.util.ListIterator;

import org.eclipse.virgo.util.common.SynchronizedListIterator;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class SynchronizedListIteratorTests {

    private static final String S0 = "0";

    private static final String S1 = "1";

    private static final String S2 = "2";

    private static final String S3 = "3";

    private final List<String> l = new ArrayList<String>();

    private ListIterator<String> li;

    private final Object monitor = new Object();

    private SynchronizedListIterator<String> sli;

    @Before
    public void setUp() {
        this.l.add(S1);
        this.l.add(S2);
        this.l.add(S3);
        this.li = this.l.listIterator();
        this.sli = new SynchronizedListIterator<String>(this.li, this.monitor);
    }

    @Test
    public void testBasicIteration() {
        assertTrue(this.sli.hasNext());
        assertEquals(S1, this.sli.next());
        assertTrue(this.sli.hasNext());
        assertEquals(S2, this.sli.next());
        assertTrue(this.sli.hasNext());
        assertEquals(S3, this.sli.next());
        assertFalse(this.sli.hasNext());
    }

    @Test
    public void testRemove() {
        assertTrue(this.sli.hasNext());
        assertEquals(S1, this.sli.next());
        this.sli.remove();
        assertEquals(S2, this.sli.next());
        assertEquals(2, this.l.size());
    }


    @Test
    public void testPrevious() {
        assertFalse(this.sli.hasPrevious());
        this.sli.next();
        assertTrue(this.sli.hasPrevious());
        assertEquals(0, this.sli.previousIndex());
        assertEquals(S1, this.sli.previous());
        assertEquals(S1, this.sli.next());
    }

    @Test
    public void testNextIndex() {
        assertEquals(0, this.sli.nextIndex());
        this.sli.next();
        assertEquals(1, this.sli.nextIndex());
    }

    @Test
    public void testAdd() {
        this.sli.add(S0);
        assertTrue(this.sli.hasPrevious());
        assertEquals(S0, this.sli.previous());
        assertEquals(S0, this.sli.next());
        assertEquals(S1, this.sli.next());
        this.sli.add(S0);
        assertEquals(S0, this.sli.previous());
        assertEquals(S0, this.sli.next());
        assertEquals(S2, this.sli.next());
    }
    
    @Test
    public void testSet() {
        this.sli.next();
        this.sli.set(S0);
        assertEquals(S0, this.sli.previous());
        assertEquals(S0, this.sli.next());
        assertEquals(S2, this.sli.next());
    }

}
