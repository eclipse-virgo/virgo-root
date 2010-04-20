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
import java.util.Iterator;

import org.eclipse.virgo.util.common.SynchronizedIterator;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class SynchronizedIteratorTests {

    private static final String S1 = "1";
    private static final String S2 = "2";
    
    private final Collection<String> l = new ArrayList<String>();
    private Iterator<String> i;
    
    private final Object monitor = new Object();
    
    private SynchronizedIterator<String> si;
    
    @Before
    public void setUp() {
        this.l.add(S1);
        this.l.add(S2);
        this.i = l.iterator();
        si = new SynchronizedIterator<String>(this.i, this.monitor);
    }

    @Test
    public void testBasicIteration() {
        assertTrue(si.hasNext());
        assertEquals(S1, si.next());
        assertTrue(si.hasNext());
        assertEquals(S2, si.next());
        assertFalse(si.hasNext());
    }
    
    @Test
    public void testRemove() {
        assertTrue(si.hasNext());
        assertEquals(S1, si.next());
        si.remove();
        assertEquals(S2, si.next());
        assertFalse(si.hasNext());
        assertEquals(1, l.size());
    }
    
}
