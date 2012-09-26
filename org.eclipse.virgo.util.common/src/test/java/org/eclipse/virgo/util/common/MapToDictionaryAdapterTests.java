/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.util.common;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;


public class MapToDictionaryAdapterTests {

    private static final String TEST_KEY = "k";

    private static final String TEST_VALUE = "v";
    
    private static final int TEST_SIZE = 2;

    private Map<String, String> mockMap;
    
    private MapToDictionaryAdapter<String, String> adapter;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        this.mockMap = createMock(Map.class);
        this.adapter = new MapToDictionaryAdapter<String, String>(this.mockMap);
    }

    @Test
    public void testSize() {
        EasyMock.expect(this.mockMap.size()).andReturn(TEST_SIZE).once();
        EasyMock.replay(this.mockMap);
        assertEquals(TEST_SIZE, this.adapter.size());
        EasyMock.verify(this.mockMap);
    }

    @Test
    public void testIsEmpty() {
        EasyMock.expect(this.mockMap.isEmpty()).andReturn(false).once();
        EasyMock.replay(this.mockMap);
        assertFalse(this.adapter.isEmpty());
        EasyMock.verify(this.mockMap);
    }

    @Test
    public void testKeys() {
        Set<String> testKeys = new HashSet<String>();
        testKeys.add(TEST_KEY);
        
        EasyMock.expect(this.mockMap.keySet()).andReturn(testKeys).once();
        EasyMock.replay(this.mockMap);
        Enumeration<String> keys = this.adapter.keys();
        
        assertTrue(keys.hasMoreElements());
        assertEquals(TEST_KEY, keys.nextElement());
        assertFalse(keys.hasMoreElements());
        
        EasyMock.verify(this.mockMap);
    }

    @Test
    public void testElements() {
        Set<String> testValues = new HashSet<String>();
        testValues.add(TEST_VALUE);
        
        EasyMock.expect(this.mockMap.values()).andReturn(testValues).once();
        EasyMock.replay(this.mockMap);
        Enumeration<String> values = this.adapter.elements();
        
        assertTrue(values.hasMoreElements());
        assertEquals(TEST_VALUE, values.nextElement());
        assertFalse(values.hasMoreElements());
        
        EasyMock.verify(this.mockMap);
    }

    @Test
    public void testGetObject() {
        EasyMock.expect(this.mockMap.get(EasyMock.eq(TEST_KEY))).andReturn(TEST_VALUE).once();
        EasyMock.replay(this.mockMap);
        assertEquals(TEST_VALUE, this.adapter.get(TEST_KEY));
        EasyMock.verify(this.mockMap);
    }

    @Test
    public void testPutKV() {
        EasyMock.expect(this.mockMap.put(EasyMock.eq(TEST_KEY), EasyMock.eq(TEST_VALUE))).andReturn(TEST_VALUE).once();
        EasyMock.replay(this.mockMap);
        assertEquals(TEST_VALUE, this.adapter.put(TEST_KEY, TEST_VALUE));
        EasyMock.verify(this.mockMap);
    }

    @Test
    public void testRemoveObject() {
        EasyMock.expect(this.mockMap.remove(EasyMock.eq(TEST_KEY))).andReturn(TEST_VALUE).once();
        EasyMock.replay(this.mockMap);
        assertEquals(TEST_VALUE, this.adapter.remove(TEST_KEY));
        EasyMock.verify(this.mockMap);
    }

}
