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

package org.eclipse.virgo.test.stubs.framework;

import static org.eclipse.virgo.test.stubs.AdditionalAsserts.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class StubServiceReferenceTests {

    private StubServiceReference<Object> ref = new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())));

    @Test
    public void initialState() {
        assertNotNull(this.ref.getProperty(Constants.SERVICE_ID));
        assertNotNull(this.ref.getProperty(Constants.SERVICE_RANKING));
        assertNotNull(this.ref.getProperty(Constants.OBJECTCLASS));
        assertEquals(3, this.ref.getPropertyKeys().length);
        assertNull(this.ref.getUsingBundles());
        assertFalse(this.ref.isAssignableTo(new StubBundle(), "testClassName"));
    }

    @Test
    public void compareSameId() {
        StubServiceReference<Object> r1 = new StubServiceReference<Object>(0L, 0, new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())));
        StubServiceReference<Object> r2 = new StubServiceReference<Object>(0L, 0, new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())));
        assertTrue(0 == r1.compareTo(r2));
        ServiceReference<?>[] array = new ServiceReference<?>[] { r2, r1 };
        Arrays.sort(array);
        assertSame(r2, array[0]);
        assertSame(r1, array[1]);
    }

    @Test
    public void compareDifferentRanking() {
        StubServiceReference<Object> r1 = new StubServiceReference<Object>(0L, 0, new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())));
        StubServiceReference<Object> r2 = new StubServiceReference<Object>(1L, 1, new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())));
        assertTrue(0 > r1.compareTo(r2));
        assertTrue(0 < r2.compareTo(r1));

        ServiceReference<?>[] array = new ServiceReference[] { r2, r1 };
        Arrays.sort(array);
        assertSame(r1, array[0]);
        assertSame(r2, array[1]);
    }

    @Test
    public void compareDifferentId() {
        StubServiceReference<Object> r1 = new StubServiceReference<Object>(0L, 0, new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())));
        StubServiceReference<Object> r2 = new StubServiceReference<Object>(1L, 0, new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())));
        assertTrue(0 > r1.compareTo(r2));
        assertTrue(0 < r2.compareTo(r1));
        ServiceReference<?>[] array = new ServiceReference[] { r2, r1 };
        Arrays.sort(array);
        assertSame(r1, array[0]);
        assertSame(r2, array[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void compareNull() {
        this.ref.compareTo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void compareNonServiceReference() {
        this.ref.compareTo(new Object());
    }

    @Test
    public void defaultProperties() {
        assertNotNull(this.ref.getProperty(Constants.SERVICE_ID));
        assertNotNull(this.ref.getProperty(Constants.SERVICE_RANKING));
        assertNotNull(this.ref.getProperty(Constants.OBJECTCLASS));
        assertEquals(3, this.ref.getPropertyKeys().length);
    }

    @Test
    public void usingBundlesZero() {
        assertNull(this.ref.getUsingBundles());
    }

    @Test
    public void usingBundlesNonZero() {
        this.ref.addUsingBundles(new StubBundle());
        assertEquals(1, this.ref.getUsingBundles().length);
    }

    @Test
    public void isAssignableToFalse() {
        assertFalse(this.ref.isAssignableTo(new StubBundle(), "testClass"));
    }

    @Test
    public void isAssignableToTrue() {
        StubBundle bundle = new StubBundle();
        this.ref.putAssignableTo(bundle, "testClass");
        assertTrue(this.ref.isAssignableTo(bundle, "testClass"));
    }

    @Test
    public void removeUsingBundles() {
        assertNull(this.ref.getUsingBundles());
        StubBundle b = new StubBundle();
        this.ref.addUsingBundles(b);
        assertEquals(1, this.ref.getUsingBundles().length);
        this.ref.removeUsingBundles(b);
        assertNull(this.ref.getUsingBundles());
    }

    @Test
    public void testHashCode() {
        StubServiceReference<Object> ref2 = new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())));
        assertFalse(31 == ref2.hashCode());
    }

    @Test
    public void testEquals() {
        assertTrue(this.ref.equals(this.ref));
        assertFalse(this.ref.equals(null));
        assertFalse(this.ref.equals(new Object()));

        assertFalse(this.ref.equals(new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(new StubBundle())))));
        assertTrue(this.ref.equals(new StubServiceReference<Object>(this.ref.getServiceRegistration())));
    }

    @Test
    public void testToString() {
        String toString = ref.toString();
        assertContains("id", toString);
        assertContains("ranking", toString);
    }
}
