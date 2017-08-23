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

package org.eclipse.virgo.test.stubs.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class ObjectClassFilterTests {

    private final ObjectClassFilter classFilter = new ObjectClassFilter(Object.class);

    private final ObjectClassFilter classNameFilter = new ObjectClassFilter(Object.class.getName());

    @Test
    public void matchServiceReference() {
        ServiceReference<Object> objectServiceReference = new ServiceReference<Object>() {

            public int compareTo(Object reference) {
                throw new UnsupportedOperationException();
            }

            public Bundle getBundle() {
                throw new UnsupportedOperationException();
            }

            public Object getProperty(String key) {
                return new String[] { Object.class.getName() };
            }

            public String[] getPropertyKeys() {
                throw new UnsupportedOperationException();
            }

            public Bundle[] getUsingBundles() {
                throw new UnsupportedOperationException();
            }

            public boolean isAssignableTo(Bundle bundle, String className) {
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(this.classFilter.match(objectServiceReference));
        assertTrue(this.classNameFilter.match(objectServiceReference));

        ServiceReference<Object> exceptionServiceReference = new ServiceReference<Object>() {

            public int compareTo(Object reference) {
                throw new UnsupportedOperationException();
            }

            public Bundle getBundle() {
                throw new UnsupportedOperationException();
            }

            public Object getProperty(String key) {
                return new String[] { Exception.class.getName() };
            }

            public String[] getPropertyKeys() {
                throw new UnsupportedOperationException();
            }

            public Bundle[] getUsingBundles() {
                throw new UnsupportedOperationException();
            }

            public boolean isAssignableTo(Bundle bundle, String className) {
                throw new UnsupportedOperationException();
            }
        };

        assertFalse(this.classFilter.match(exceptionServiceReference));
        assertFalse(this.classNameFilter.match(exceptionServiceReference));
    }

    @Test
    public void matches() {
        Map<String, String[]> classNameMap = new HashMap<String, String[]>();
        classNameMap.put(Constants.OBJECTCLASS, new String[] { Object.class.getName(), Object.class.getName() });

        assertTrue(this.classFilter.matches(classNameMap));
    }

    @Test(expected = NullPointerException.class)
    public void matchesWithEmptyMap() {
        this.classFilter.matches(new HashMap<String, Object>());
    }

    @Test
    public void matchDictionaryTrue() {
        Dictionary<String, Object> d1 = new Hashtable<>();
        d1.put(Constants.OBJECTCLASS, new String[] { Object.class.getName() });
        assertTrue(this.classFilter.match(d1));
        assertTrue(this.classFilter.matchCase(d1));

        assertTrue(this.classNameFilter.match(d1));
        assertTrue(this.classNameFilter.matchCase(d1));
    }

    @Test
    public void matchDictionaryFalse() {
        Dictionary<String, Object> d1 = new Hashtable<>();
        d1.put(Constants.OBJECTCLASS, new String[] { Exception.class.getName() });
        assertFalse(this.classFilter.match(d1));
        assertFalse(this.classFilter.matchCase(d1));

        assertFalse(this.classNameFilter.match(d1));
        assertFalse(this.classNameFilter.matchCase(d1));
    }

    @Test
    public void testToString() {
        assertEquals("(objectClass=java.lang.Object)", this.classFilter.toString());
        assertEquals("(objectClass=java.lang.Object)", this.classNameFilter.toString());
    }

    @Test
    public void hashCodeEqualsToStringsHashCode() {
        assertEquals(this.classFilter.hashCode(), this.classFilter.toString().hashCode());
        assertEquals(this.classFilter.hashCode(), this.classNameFilter.toString().hashCode());
    }
}
