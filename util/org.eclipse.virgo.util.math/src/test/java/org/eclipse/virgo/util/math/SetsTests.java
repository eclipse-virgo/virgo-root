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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

/**
 */
public class SetsTests {

    @Test public void difference() {
        Set<String> a = Sets.asSet(new String[]{"a", "b", "c"});
        Set<String> b = Sets.asSet(new String[]{"a", "c"});
        Set<String> r = Sets.difference(a, b);
        assertEquals(1, r.size());
        assertTrue(r.contains("b"));
    }

    @Test public void intersection() {
        Set<String> a = Sets.asSet(new String[]{"a", "b", "c", "d"});
        Set<String> b = Sets.asSet(new String[]{"a", "c", "e"});
        Set<String> r = Sets.intersection(a, b);
        Set<String> s = Sets.intersection(b, a);
        assertEquals(r,s);
        assertEquals(2, r.size());
        assertTrue(r.contains("a")); 
        assertTrue(r.contains("c"));
    }
}
