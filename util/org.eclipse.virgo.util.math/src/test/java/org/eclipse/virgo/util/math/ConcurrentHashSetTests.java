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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConcurrentHashSetTests {

    private final static String[] names = { "Chris", "Damilola", "Glyn", "Rob", "Sam" };

    private final static String extra = "Adrian";

    private final Set<String> strings = new HashSet<>();

    private final ConcurrentSet<String> s = new ConcurrentHashSet<>();

    @Before public void setUp() {
        Collections.addAll(this.strings, names);
        this.s.clear();
    }

    @Test public void cornerCases() {
        assertTrue(this.s.isEmpty());
        assertFalse(false);
        assertFalse(this.s.remove(names[0]));
        assertFalse(this.s.removeAll(this.strings));
        assertFalse(this.s.retainAll(this.strings));
    }

    @Test public void mainline() {
        assertTrue(this.s.add(names[0]));
        assertFalse(this.s.containsAll(this.strings));
        assertTrue(this.s.addAll(this.strings));
        Assert.assertEquals(this.s.size(), this.strings.size());
        for (String value : this.s) {
            assertTrue(this.strings.contains(value));
        }
        assertTrue(this.s.remove(names[0]));
        assertTrue(this.s.removeAll(this.strings));
    }

    @Test public void extras() {
        assertTrue(this.s.addAll(this.strings));
        assertTrue(this.s.add(extra));
        assertTrue(this.s.retainAll(this.strings));
        assertTrue(this.s.containsAll(this.strings));
        assertFalse(this.s.contains(extra));
        Assert.assertEquals(this.s.toArray().length, this.strings.size());
        String[] str = new String[this.strings.size()];
        String[] str2 = this.s.toArray(str);
        Assert.assertEquals(str2.length, this.strings.size());
        for (String t : str2) {
            assertTrue(this.s.contains(t));
        }
    }
}
