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

import org.eclipse.virgo.util.math.Range;
import org.junit.Assert;
import org.junit.Test;

public class RangeTests {

    @Test
    public void testIntRangeIncInc() {
        Range<Integer> r = new Range<Integer>(0, true, 2, true);
        Assert.assertTrue(r.contains(0));
        Assert.assertTrue(r.contains(1));
        Assert.assertTrue(r.contains(2));
        Assert.assertFalse(r.contains(3));
    }

    @Test
    public void testIntRangeIncExc() {
        Range<Integer> r = new Range<Integer>(0, true, 2, false);
        Assert.assertTrue(r.contains(0));
        Assert.assertTrue(r.contains(1));
        Assert.assertFalse(r.contains(2));
        Assert.assertFalse(r.contains(3));
    }

    @Test
    public void testIntRangeExcInc() {
        Range<Integer> r = new Range<Integer>(0, false, 2, true);
        Assert.assertFalse(r.contains(0));
        Assert.assertTrue(r.contains(1));
        Assert.assertTrue(r.contains(2));
        Assert.assertFalse(r.contains(3));
    }

    @Test
    public void testIntRangeExcExc() {
        Range<Integer> r = new Range<Integer>(0, false, 2, false);
        Assert.assertFalse(r.contains(0));
        Assert.assertTrue(r.contains(1));
        Assert.assertFalse(r.contains(2));
        Assert.assertFalse(r.contains(3));
    }

    @Test
    public void testIntRangeEmpty() {
        Range<Integer> r = new Range<Integer>(1, true, 0, true);
        Assert.assertFalse(r.contains(0));
        Assert.assertFalse(r.contains(1));
    }

    @Test
    public void testRangeToString() {
        Range<Integer> closed0_closed2 = new Range<Integer>(0, true, 2, true);
        Assert.assertEquals("Unexpected toString result", "[0,2]", closed0_closed2.toString());
        
        Range<Integer> open0_closed2 = new Range<Integer>(0, false, 2, true);
        Assert.assertEquals("Unexpected toString result", "(0,2]", open0_closed2.toString());
        
        Range<Integer> closed0_open2 = new Range<Integer>(0, true, 2, false);
        Assert.assertEquals("Unexpected toString result", "[0,2)", closed0_open2.toString());
        
        Range<Integer> open0_open2 = new Range<Integer>(0, false, 2, false);
        Assert.assertEquals("Unexpected toString result", "(0,2)", open0_open2.toString());
        
        Range<Integer> empty = new Range<Integer>(1, true, 0, true);
        Assert.assertEquals("Unexpected toString result", "[1,0]", empty.toString());
    }
}
