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

package org.eclipse.virgo.nano.serviceability;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.virgo.nano.serviceability.Assert;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.junit.Test;


/**
 */
public class AssertTests {

    @Test
    public void testHasLength() {
        try {
            Assert.hasLength(null, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        try {
            Assert.hasLength("", "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Assert.hasLength("x", "blah");
    }

    @Test
    public void testIsAssignable() {
        try {
            Assert.isAssignable(String.class, Object.class, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        try {
            Assert.isAssignable(null, Object.class, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        try {
            Assert.isAssignable(Object.class, null, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Assert.isAssignable(Object.class, Object.class, "blah");
        Assert.isAssignable(Object.class, String.class, "blah");
    }

    @Test
    public void testIsInstanceOf() {
        try {
            Assert.isInstanceOf(Integer.class, "", "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        try {
            Assert.isInstanceOf(null, "", "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        try {
            Assert.isInstanceOf(Integer.class, null, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Assert.isInstanceOf(String.class, "", "blah");
    }

    @Test
    public void testIsNull() {
        try {
            Assert.isNull("", "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Assert.isNull(null, "blah");
    }

    @Test
    public void testIsTrue() {
        try {
            Assert.isTrue(false, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Assert.isTrue(true, "blah");
    }
    
    @Test
    public void testIsFalse() {
    	try {
    		Assert.isFalse(true, "blah");
    		org.junit.Assert.assertTrue(false);
    	} catch (FatalAssertionException e) {
    	}
    	Assert.isFalse(false, "blah");
    }
    
    @Test
    public void testNotEmptyCollection() {
        try {
            Assert.notEmpty(new HashSet<String>(), "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        try {
            Assert.notEmpty((Collection<String>)null, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Set<String> a = new HashSet<String>();
        a.add("x");
        Assert.notEmpty(a, "blah");
    }
    
    @Test
    public void testNotEmptyMap() {
        try {
            Assert.notEmpty(new HashMap<String, String>(), "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        try {
            Assert.notEmpty((Map<String, String>)null, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Map<String, String> a = new HashMap<String, String>();
        a.put("k", "v");
        Assert.notEmpty(a, "blah");
    }
    
    @Test
    public void testNotEmptyArray() {
        try {
            Object[] a = {};
            Assert.notEmpty(a, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        try {
            Assert.notEmpty((Object[])null, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Object[] a = {"x"};
        Assert.notEmpty(a, "blah");
    }
    
    @Test
    public void testNotNull() {
        try {
            Assert.notNull(null, "blah");
            org.junit.Assert.assertTrue(false);
        } catch (FatalAssertionException e) {
        }
        Assert.notNull("x", "blah");
    }
}
