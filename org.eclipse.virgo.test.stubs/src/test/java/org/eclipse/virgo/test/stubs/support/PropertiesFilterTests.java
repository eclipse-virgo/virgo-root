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
import java.util.TreeMap;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class PropertiesFilterTests {

    @Test
    public void matchServiceReference() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("testKey", "testValue");
        PropertiesFilter filter = new PropertiesFilter(properties);
        assertFalse(filter.match(new ServiceReference<Object>() {

            public int compareTo(Object reference) {
                throw new UnsupportedOperationException();
            }

            public Bundle getBundle() {
                throw new UnsupportedOperationException();
            }

            public Object getProperty(String key) {
                return null;
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
        }));

        assertFalse(filter.match(new ServiceReference<Object>() {

            public int compareTo(Object reference) {
                throw new UnsupportedOperationException();
            }

            public Bundle getBundle() {
                throw new UnsupportedOperationException();
            }

            public Object getProperty(String key) {
                return "badValue";
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
        }));

        assertTrue(filter.match(new ServiceReference<Object>() {

            public int compareTo(Object reference) {
                throw new UnsupportedOperationException();
            }

            public Bundle getBundle() {
                throw new UnsupportedOperationException();
            }

            public Object getProperty(String key) {
                return "testValue";
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
        }));
    }

    @Test
    public void matchesSameProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("testKey", "testValue");
        PropertiesFilter filter = new PropertiesFilter(properties);

        assertTrue(filter.matches(properties));

        properties.put("newTestKey", "newTestValue");
        assertTrue(filter.matches(properties));

        properties.remove("testKey"); //removes from filter
        assertTrue(filter.matches(properties));
    }

    @Test
    public void matchesNewProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("testKey", "testValue");
        PropertiesFilter filter = new PropertiesFilter(properties);

        Map<String, Object> newProperties = new HashMap<String, Object>();
        newProperties.put("testKey", "testValue");

        assertTrue(filter.matches(newProperties));

        newProperties.put("newTestKey", "newTestValue");
        assertTrue(filter.matches(newProperties));

        newProperties.remove("testKey");
        assertFalse(filter.matches(newProperties));
    }

    @Test(expected = NullPointerException.class)
    public void matchesNull() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("testKey", "testValue");
        PropertiesFilter filter = new PropertiesFilter(properties);
        filter.matches(null);
    }

    @Test
    public void matchDictionary() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("testKey", "testValue");
        PropertiesFilter filter = new PropertiesFilter(properties);

        Dictionary<String, Object> d1 = new Hashtable<>();
        assertFalse(filter.match(d1));
        assertFalse(filter.matchCase(d1));

        Dictionary<String, Object> d2 = new Hashtable<>();
        d2.put("testKey", "badValue");
        assertFalse(filter.match(d2));
        assertFalse(filter.matchCase(d2));

        Dictionary<String, Object> d3 = new Hashtable<>();
        d3.put("testKey", "testValue");
        assertTrue(filter.match(d3));
        assertTrue(filter.matchCase(d3));
    }

    @Test
    public void getFilterStringEmpty() {
        Map<String, Object> properties = new HashMap<String, Object>();
        PropertiesFilter filter = new PropertiesFilter(properties);
        assertEquals("", filter.getFilterString());
    }

    @Test
    public void getFilterStringOne() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("key", "value");
        PropertiesFilter filter = new PropertiesFilter(properties);
        assertEquals("(key=value)", filter.toString());
    }

    @Test
    public void getFilterStringMoreThanOne() {
        Map<String, Object> properties = new TreeMap<String, Object>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        PropertiesFilter filter = new PropertiesFilter(properties);
        assertEquals("(&(key1=value1)(key2=value2))", filter.toString());
    }
    
    @Test
    public void hashCodeEqualsToStringsHashCode() {
        Map<String, Object> properties = new TreeMap<String, Object>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        PropertiesFilter filter = new PropertiesFilter(properties);
        
        assertEquals(filter.hashCode(), filter.toString().hashCode());
    }

    @Test
    public void fromFilterStringEmpty() throws InvalidSyntaxException {
        String filterString = "";
        PropertiesFilter filter = new PropertiesFilter(filterString);
        assertEquals(filterString, filter.getFilterString());
    }

    @Test
    public void fromFilterStringOne() throws InvalidSyntaxException {
        String filterString = "(key=value)";
        PropertiesFilter filter = new PropertiesFilter(filterString);
        assertEquals(filterString, filter.getFilterString());
    }

    @Test
    public void fromFilterStringMoreThanOne() throws InvalidSyntaxException {
        String filterString = "(&(key1=value1)(key2=value2))";
        PropertiesFilter filter = new PropertiesFilter(filterString);
        assertEquals(filterString, filter.getFilterString());
    }
}
