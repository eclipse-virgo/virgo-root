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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.virgo.util.math.ConcurrentHashSet;
import org.eclipse.virgo.util.math.ConcurrentSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentHashSetTests {

    private final static String[] names = { "Chris", "Damilola", "Glyn", "Rob", "Sam" };

    private final static String extra = "Adrian";

    private final Set<String> strings = new HashSet<String>();

    final ConcurrentSet<String> s = new ConcurrentHashSet<String>();

    @Before public void setUp() throws Exception {
        for (final String s : names) {
            this.strings.add(s);
        }
        this.s.clear();
    }

    @After public void tearDown() throws Exception {
    }

    @Test public void cornerCases() {
        Assert.assertTrue(this.s.isEmpty());
        Assert.assertFalse(this.s.contains(names[0]));
        Assert.assertFalse(this.s.remove(names[0]));
        Assert.assertFalse(this.s.removeAll(this.strings));
        Assert.assertFalse(this.s.retainAll(this.strings));
    }

    @Test public void mainline() {
        Assert.assertTrue(this.s.add(names[0]));
        Assert.assertFalse(this.s.containsAll(this.strings));
        Assert.assertTrue(this.s.addAll(this.strings));
        Assert.assertEquals(this.s.size(), this.strings.size());
        Iterator<String> i = this.s.iterator();
        while (i.hasNext()) {
            Assert.assertTrue(this.strings.contains(i.next()));
        }
        Assert.assertTrue(this.s.remove(names[0]));
        Assert.assertTrue(this.s.removeAll(this.strings));
    }

    @Test public void extras() {
        Assert.assertTrue(this.s.addAll(this.strings));
        Assert.assertTrue(this.s.add(extra));
        Assert.assertTrue(this.s.retainAll(this.strings));
        Assert.assertTrue(this.s.containsAll(this.strings));
        Assert.assertFalse(this.s.contains(extra));
        Assert.assertEquals(this.s.toArray().length, this.strings.size());
        String[] str = new String[this.strings.size()];
        String[] str2 = this.s.toArray(str);
        Assert.assertEquals(str2.length, this.strings.size());
        for (String t : str2) {
            Assert.assertTrue(this.s.contains(t));
        }
    }
}
