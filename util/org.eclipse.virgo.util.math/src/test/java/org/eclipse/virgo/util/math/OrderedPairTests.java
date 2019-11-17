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

import org.junit.Test;

import static org.junit.Assert.*;

public class OrderedPairTests {

    @Test
    public void getters() {
        String orange = "orange";
        String blue = "blue";
        OrderedPair<String, String> o = new OrderedPair<>(orange, blue);

        assertSame(orange, o.getFirst());
        assertSame(blue, o.getSecond());
        assertEquals("toString value unexpected", "(orange, blue)", o.toString());
    }

    @Test
    public void equalsAndHashCode() {
        OrderedPair<Integer, Integer> alpha = new OrderedPair<>(1, 2);
        OrderedPair<String, String> bravo = new OrderedPair<>("first", "second");
        OrderedPair<String, String> charlie = new OrderedPair<>("orange", "blue");
        OrderedPair<String, String> delta = new OrderedPair<>("orange", "blue");

        assertNotEqualsAndHashCodeNotEqual(alpha, bravo);
        assertEqualsAndHashCodeEqual(alpha, alpha);
        assertNotEqualsAndHashCodeNotEqual(bravo, charlie);
        assertEqualsAndHashCodeEqual(charlie, delta);

        assertNotEquals(null, alpha);
        assertNotEquals(1, alpha);
    }

    @Test
    public void nullElements() {
        OrderedPair<Integer, Integer> abel = new OrderedPair<>(null, null);
        OrderedPair<Integer, Integer> baker = new OrderedPair<>(null, null);
        OrderedPair<Integer, Integer> charlie = new OrderedPair<>(1, null);
        OrderedPair<Integer, Integer> dog = new OrderedPair<>(1, null);
        OrderedPair<Integer, Integer> easy = new OrderedPair<>(null, 2);
        OrderedPair<Integer, Integer> fox = new OrderedPair<>(null, 2);
        OrderedPair<Integer, String> gorilla = new OrderedPair<>(null, null);
        
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
        assertEquals(o, p);
        assertEquals(o.hashCode(), p.hashCode());
        assertEquals(o.toString(), p.toString());
    }

    private void assertNotEqualsAndHashCodeNotEqual(OrderedPair<?, ?> o, OrderedPair<?, ?> p) {
        assertNotEquals(o, p);
        assertNotEquals(o.hashCode(), p.hashCode());
        assertNotEquals(o.toString(), p.toString());
    }
}
