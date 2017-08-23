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
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.virgo.util.math.ConcurrentMapRelation;
import org.eclipse.virgo.util.math.ConcurrentRelation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentHashMapConcurrentHashSetRelationTests {

    private static final String[] forename = { "G", "S", "D", "P", "S", "P" };

    private static final String[] surname = { "N", "N", "N", "N", "B", "B" };

    private ConcurrentRelation<String, String> name;

    @Before public void setUp() throws Exception {
        this.name = new ConcurrentMapRelation<String, String>();
    }

    @After public void tearDown() throws Exception {
    }

    @Test public void testBasicOperations() {
        for (int i = 0; i < forename.length; i++) {
            Assert.assertFalse(this.name.contains(forename[i], surname[i]));
        }
        for (int i = 0; i < forename.length; i++) {
            Assert.assertTrue(this.name.add(forename[i], surname[i]));
        }
        for (int i = 0; i < forename.length; i++) {
            Assert.assertFalse(this.name.add(forename[i], surname[i]));
        }
        for (int i = 0; i < forename.length; i++) {
            Assert.assertTrue(this.name.contains(forename[i], surname[i]));
        }
        Assert.assertTrue(this.name.remove(forename[0], surname[0]));
        Assert.assertFalse(this.name.contains(forename[0], surname[0]));
        Assert.assertFalse(this.name.remove(forename[0], surname[0]));
    }

    @Test public void testDomain() {
        for (int i = 0; i < forename.length; i++) {
            Assert.assertTrue(this.name.add(forename[i], surname[i]));
        }
        Set<String> domain = this.name.dom();
        // Check forname is a subset of domain
        for (int i = 0; i < forename.length; i++) {
            Assert.assertTrue(domain.contains(forename[i]));
        }
        // Check domain is a subset of forname
        for (String d : domain) {
            boolean found = false;
            for (int i = 0; i < forename.length; i++) {
                if (d.equals(forename[i])) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
        }
    }

    @Test public void testRange() {
        for (int i = 0; i < forename.length; i++) {
            Assert.assertTrue(this.name.add(forename[i], surname[i]));
        }
        Set<String> range = this.name.ran();
        // Check surname is a subset of range
        for (int i = 0; i < surname.length; i++) {
            Assert.assertTrue(range.contains(surname[i]));
        }
        // Check range is a subset of surname
        for (String r : range) {
            boolean found = false;
            for (int i = 0; i < surname.length; i++) {
                if (r.equals(surname[i])) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
        }
    }

    @Test public void testRelationalImage() {
        for (int i = 0; i < forename.length; i++) {
            Assert.assertTrue(this.name.add(forename[i], surname[i]));
        }
        Set<String> xset = new HashSet<String>();
        xset.add("S");
        xset.add("Q");
        Set<String> expectedRelationalImage = new HashSet<String>();
        expectedRelationalImage.add("N");
        expectedRelationalImage.add("B");
        Assert.assertEquals(expectedRelationalImage, this.name.relationalImage(xset));
    }

    @Test public void testDomSubtract() {
        for (int i = 0; i < forename.length; i++) {
            Assert.assertTrue(this.name.add(forename[i], surname[i]));
        }
        Set<String> xset = new HashSet<String>();
        xset.add("S");
        xset.add("P");
        this.name.domSubtract(xset);
        Set<String> expectedRange = new HashSet<String>();
        expectedRange.add("N");
        Assert.assertEquals(expectedRange, this.name.ran());
    }
}
