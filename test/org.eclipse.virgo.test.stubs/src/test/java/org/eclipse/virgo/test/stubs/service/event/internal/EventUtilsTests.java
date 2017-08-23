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

package org.eclipse.virgo.test.stubs.service.event.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;
import org.osgi.service.event.Event;

/**
 */
public class EventUtilsTests {
    
    @Test
    public void equalObjectArrays() {
        assertTrue(EventUtils.arraysAreEqual(new Object[] {new Integer(5), Boolean.FALSE, "apple"}, new Object[] {new Integer(5), Boolean.FALSE, "apple"}));
    }
    
    @Test
    public void unequalObjectArrays() {
        assertFalse(EventUtils.arraysAreEqual(new Object[] {new Integer(5), Boolean.FALSE, "apple"}, new Object[] {new Integer(578), Boolean.FALSE, "orange"}));
    }
    
    @Test
    public void equalBooleanArrays() {
        assertTrue(EventUtils.arraysAreEqual(new boolean[] {false, true}, new boolean[] {false, true}));
    }
    
    @Test
    public void unequalBooleanArrays() {
        assertFalse(EventUtils.arraysAreEqual(new boolean[] {false, true}, new boolean[] {true, true}));
    }

    @Test
    public void equalByteArrays() {
        assertTrue(EventUtils.arraysAreEqual(new byte[] {1, 2, 3}, new byte[] {1, 2, 3}));
    }
    
    @Test
    public void unequalByteArrays() {
        assertFalse(EventUtils.arraysAreEqual(new byte[] {1, 2, 3}, new byte[] {1, 3}));
    }
    
    @Test
    public void equalCharArrays() {
        assertTrue(EventUtils.arraysAreEqual(new char[] {'a', '1'}, new char[] {'a', '1'}));
    }
    
    @Test
    public void unequalCharArrays() {
        assertFalse(EventUtils.arraysAreEqual(new char[] {'a', '1'}, new char[] {'a'}));
    }
    
    @Test
    public void equalDoubleArrays() {
        assertTrue(EventUtils.arraysAreEqual(new double[] {1.0d, 3.45673d}, new double[] {1.0d, 3.45673d}));
    }
    
    @Test
    public void unequalDoubleArrays() {
        assertFalse(EventUtils.arraysAreEqual(new double[] {1.0d, 3.45673d}, new double[] {1.0d, 30.45673d}));
    }
    
    @Test
    public void equalFloatArrays() {
        assertTrue(EventUtils.arraysAreEqual(new float[] {1.0f, 3.45673f}, new float[] {1.0f, 3.45673f}));
    }
    
    @Test
    public void unequalFloatArrays() {
        assertFalse(EventUtils.arraysAreEqual(new float[] {1.0f, 3.45673f}, new float[] {1.0f, 0.45673f}));
    }
    
    @Test
    public void equalIntArrays() {
        assertTrue(EventUtils.arraysAreEqual(new int[] {-5, 0, 93}, new int[] {-5, 0, 93}));
    }
    
    @Test
    public void unequalIntArrays() {
        assertFalse(EventUtils.arraysAreEqual(new int[] {-5, 0, 93}, new int[] {-5, 0, 9356}));
    }
    
    @Test
    public void equalLongArrays() {
        assertTrue(EventUtils.arraysAreEqual(new long[] {-5, 0, 93}, new long[] {-5, 0, 93}));
    }
    
    @Test
    public void unequalLongArrays() {
        assertFalse(EventUtils.arraysAreEqual(new long[] {-5, 0, 93}, new long[] {-5, 0, 9356}));
    }
    
    @Test
    public void equalShortArrays() {
        assertTrue(EventUtils.arraysAreEqual(new short[] {-5, 0, 93}, new short[] {-5, 0, 93}));
    }
    
    @Test
    public void unequalShortArrays() {
        assertFalse(EventUtils.arraysAreEqual(new short[] {-5, 0, 93}, new short[] {-5, 0, 9356}));
    }
    
    @Test
    public void eventsWithDifferentTopicsAreNotEqual() {
        assertFalse(EventUtils.eventsAreEqual(new Event("foo", (Map<String, ?>)null), new Event("bar", (Map<String, ?>)null)));
    }
    
    @Test
    public void eventsWithMatchingTopicsAndNoPropertiesAreEqual() {
        assertTrue(EventUtils.eventsAreEqual(new Event("foo", (Map<String, ?>)null), new Event("foo", (Map<String, ?>)null)));
    }
    
    @Test
    public void eventsWithMatchingTopicsAndMatchingPropertiesAreEqual() {
        assertTrue(EventUtils.eventsAreEqual(new Event("foo", createProperties()), new Event("foo", createProperties())));
    }
    
    @Test
    public void eventsWithMatchingTopicsAndDifferentNumberOfPropertiesAreNotEqual() {
        Dictionary<String, ?> properties = createProperties();
        properties.remove("byteArray");
        
        assertFalse(EventUtils.eventsAreEqual(new Event("foo", createProperties()), new Event("foo", properties)));
    }
    
    @Test
    public void eventsWithMatchingTopicsAndDifferentPropertiesAreNotEqual() {
        Dictionary<String, Object> properties = createProperties();
        properties.put("byteArray", new byte[] {6});
        
        assertFalse(EventUtils.eventsAreEqual(new Event("foo", createProperties()), new Event("foo", properties)));
        
        properties = createProperties();
        properties.put("object", "bravo");
        
        assertFalse(EventUtils.eventsAreEqual(new Event("foo", createProperties()), new Event("foo", properties)));
    }
    
    private Dictionary<String, Object> createProperties() {
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        
        properties.put("object", "alpha");
        properties.put("booleanArray", new boolean[] {false, true});
        properties.put("byteArray", new byte[] {1, 2});
        properties.put("charArray", new char[] {'a', 'b'});
        properties.put("doubleArray", new double[] {1.0d});
        properties.put("floatArray", new float[] {2.45f});
        properties.put("intArray", new int[] {1, 2, 3});
        properties.put("longArray", new long[] {1L, 2L});
        properties.put("shortArray", new short[] {5, 9, 18});
        
        return properties;
    }
}
