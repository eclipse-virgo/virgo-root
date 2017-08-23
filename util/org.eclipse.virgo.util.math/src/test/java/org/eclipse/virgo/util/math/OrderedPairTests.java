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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import org.eclipse.virgo.util.math.OrderedPair;
import org.junit.Test;

public class OrderedPairTests {

    @Test
    public void getters() {
        String orange = "orange";
        String blue = "blue";
        OrderedPair<String, String> o = new OrderedPair<String, String>(orange, blue);

        assertSame(orange, o.getFirst());
        assertSame(blue, o.getSecond());
        assertEquals("toString value unexpected", "(orange, blue)", o.toString());
    }

    @Test
    public void equalsAndHashCode() {
        OrderedPair<Integer, Integer> alpha = new OrderedPair<Integer, Integer>(1, 2);
        OrderedPair<String, String> bravo = new OrderedPair<String, String>("first", "second");
        OrderedPair<String, String> charlie = new OrderedPair<String, String>("orange", "blue");
        OrderedPair<String, String> delta = new OrderedPair<String, String>("orange", "blue");

        assertNotEqualsAndHashCodeNotEqual(alpha, bravo);
        assertEqualsAndHashCodeEqual(alpha, alpha);
        assertNotEqualsAndHashCodeNotEqual(bravo, charlie);
        assertEqualsAndHashCodeEqual(charlie, delta);

        assertFalse(alpha.equals(null));
        assertFalse(alpha.equals(new Integer(1)));
    }

    @Test
    public void nullElements() {
        OrderedPair<Integer, Integer> abel = new OrderedPair<Integer, Integer>(null, null);
        OrderedPair<Integer, Integer> baker = new OrderedPair<Integer, Integer>(null, null);
        OrderedPair<Integer, Integer> charlie = new OrderedPair<Integer, Integer>(1, null);
        OrderedPair<Integer, Integer> dog = new OrderedPair<Integer, Integer>(1, null);
        OrderedPair<Integer, Integer> easy = new OrderedPair<Integer, Integer>(null, 2);
        OrderedPair<Integer, Integer> fox = new OrderedPair<Integer, Integer>(null, 2);
        OrderedPair<Integer, String> gorilla = new OrderedPair<Integer, String>(null, null);
        
        assertSame(null, abel.getFirst());
        assertSame(null, abel.getSecond());
        assertEquals("toString value unexpected", "(null, null)", abel.toString());
        
        assertEqualsAndHashCodeEqual(abel, baker);
        assertEqualsAndHashCodeEqual(charlie, dog);
        assertEqualsAndHashCodeEqual(easy, fox);

        assertNotEqualsAndHashCodeNotEqual(abel, charlie);
        assertNotEqualsAndHashCodeNotEqual(charlie, easy);
        assertNotEqualsAndHashCodeNotEqual(abel, fox);

        assertEqualsAndHashCodeEqual(abel, gorilla);
    }

    private void assertEqualsAndHashCodeEqual(OrderedPair<?, ?> o, OrderedPair<?, ?> p) {
        assertTrue(o.equals(p));
        assertTrue(o.hashCode() == p.hashCode());
        assertEquals(o.toString(), p.toString());
    }

    private void assertNotEqualsAndHashCodeNotEqual(OrderedPair<?, ?> o, OrderedPair<?, ?> p) {
        assertFalse(o.equals(p));
        assertFalse(o.hashCode() == p.hashCode());
        assertFalse(o.toString().equals(p.toString()));
    }
}
