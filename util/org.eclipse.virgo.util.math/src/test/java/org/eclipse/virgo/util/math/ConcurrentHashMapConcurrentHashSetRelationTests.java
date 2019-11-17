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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConcurrentHashMapConcurrentHashSetRelationTests {

    private static final String[] forename = { "G", "S", "D", "P", "S", "P" };

    private static final String[] surname = { "N", "N", "N", "N", "B", "B" };

    private ConcurrentRelation<String, String> name;

    @Before public void setUp() {
        this.name = new ConcurrentMapRelation<>();
    }

    @Test public void testBasicOperations() {
        for (int i = 0; i < forename.length; i++) {
            assertFalse(this.name.contains(forename[i], surname[i]));
        }
        for (int i = 0; i < forename.length; i++) {
            assertTrue(this.name.add(forename[i], surname[i]));
        }
        for (int i = 0; i < forename.length; i++) {
            assertFalse(this.name.add(forename[i], surname[i]));
        }
        for (int i = 0; i < forename.length; i++) {
            assertTrue(this.name.contains(forename[i], surname[i]));
        }
        assertTrue(this.name.remove(forename[0], surname[0]));
        assertFalse(this.name.contains(forename[0], surname[0]));
        assertFalse(this.name.remove(forename[0], surname[0]));
    }

    @Test public void testDomain() {
        for (int i = 0; i < forename.length; i++) {
            assertTrue(this.name.add(forename[i], surname[i]));
        }
        Set<String> domain = this.name.dom();
        // Check forname is a subset of domain
        for (String s : forename) {
            assertTrue(domain.contains(s));
        }
        // Check domain is a subset of forname
        for (String d : domain) {
            boolean found = false;
            for (String s : forename) {
                if (d.equals(s)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Test public void testRange() {
        for (int i = 0; i < forename.length; i++) {
            assertTrue(this.name.add(forename[i], surname[i]));
        }
        Set<String> range = this.name.ran();
        // Check surname is a subset of range
        for (String s : surname) {
            assertTrue(range.contains(s));
        }
        // Check range is a subset of surname
        for (String r : range) {
            boolean found = false;
            for (String s : surname) {
                if (r.equals(s)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Test public void testRelationalImage() {
        for (int i = 0; i < forename.length; i++) {
            assertTrue(this.name.add(forename[i], surname[i]));
        }
        Set<String> xset = new HashSet<>();
        xset.add("S");
        xset.add("Q");
        Set<String> expectedRelationalImage = new HashSet<>();
        expectedRelationalImage.add("N");
        expectedRelationalImage.add("B");
        assertEquals(expectedRelationalImage, this.name.relationalImage(xset));
    }

    @Test public void testDomSubtract() {
        for (int i = 0; i < forename.length; i++) {
            assertTrue(this.name.add(forename[i], surname[i]));
        }
        Set<String> xset = new HashSet<>();
        xset.add("S");
        xset.add("P");
        this.name.domSubtract(xset);
        Set<String> expectedRange = new HashSet<>();
        expectedRange.add("N");
        assertEquals(expectedRange, this.name.ran());
    }
}
