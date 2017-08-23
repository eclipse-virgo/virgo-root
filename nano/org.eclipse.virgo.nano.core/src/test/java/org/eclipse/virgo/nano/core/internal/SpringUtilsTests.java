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

package org.eclipse.virgo.nano.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.junit.Test;
import org.osgi.framework.Version;

public class SpringUtilsTests {

    @Test
    public void successfulGetSpringContextHeader() throws Exception {
        Dictionary<String, String> dict = new Hashtable<String, String>();
    	dict.put("Spring-Context", "testValue");
    	dict.put("aHeader", "aValue");
    	
    	String result = SpringUtils.getSpringContextHeader(dict);
    	
    	assertEquals("testValue", result);
    }

    @Test
    public void failedGetSpringContextHeader() {
    	
        Dictionary<String, String> dict = new Hashtable<String, String>();
	    dict.put("aHeader", "aValue");
	
	    String result = SpringUtils.getSpringContextHeader(dict);
	
	    assertEquals(null, result);
    }
    
    @Test
    public void successfulGetBundleBlueprintHeader() throws Exception {
        Dictionary<String, String> dict = new Hashtable<String, String>();
        dict.put("Bundle-Blueprint", "testValue");
        dict.put("aHeader", "aValue");
        
        String result = SpringUtils.getBundleBlueprintHeader(dict);
        
        assertEquals("testValue", result);
    }

    @Test
    public void failedGetBundleBlueprintHeader() {
        
        Dictionary<String, String> dict = new Hashtable<String, String>();
        dict.put("aHeader", "aValue");
    
        String result = SpringUtils.getBundleBlueprintHeader(dict);
    
        assertEquals(null, result);
    }
    
    @Test
    public void testIsEmptyCheck() {
        
        Object[] emptyArray = {};
        boolean isEmpty = SpringUtils.isArrayEmpty(emptyArray);
        assertEquals(true, isEmpty);
        
        Object[] nonEmptyArray = {"aString"};
        isEmpty = SpringUtils.isArrayEmpty(nonEmptyArray);
        assertEquals(false, isEmpty);
    }
    
    @Test
    public void testGetSpringContextHeaderLocations() {
        //try single location
        Dictionary<String, String> dict = new Hashtable<String, String>();
        dict.put("Spring-Context", "testValue");
        dict.put("aHeader", "aValue");
        
        String[] locations = SpringUtils.getSpringContextHeaderLocations(dict);
        assertEquals(1, locations.length);
        assertEquals("testValue", locations[0]);
        
        //try with 1+ locations
        dict = new Hashtable<String, String>();
        dict.put("Spring-Context", "testValue1,testValue2");
        dict.put("aHeader", "aValue");
        
        locations = SpringUtils.getSpringContextHeaderLocations(dict);
        assertEquals(2, locations.length);
        assertEquals("testValue1", locations[0]);
        assertEquals("testValue2", locations[1]);
    }
    
    @Test
    public void testGetBundleBlueprintHeaderLocations() {
        //try single location
        Dictionary<String, String> dict = new Hashtable<String, String>();
        dict.put("Bundle-Blueprint", "testValue");
        dict.put("aHeader", "aValue");

        String[] locations = SpringUtils.getSpringContextHeaderLocations(dict);
        assertEquals(1, locations.length);
        assertEquals("testValue", locations[0]);
        
        //try with 1+ locations
        dict = new Hashtable<String, String>();
        dict.put("Bundle-Blueprint", "testValue1,testValue2");
        dict.put("aHeader", "aValue");
        
        locations = SpringUtils.getSpringContextHeaderLocations(dict);
        assertEquals(2, locations.length);
        assertEquals("testValue1", locations[0]);
        assertEquals("testValue2", locations[1]);
    }

    
    @Test
    public void testIsSpringPoweredBundle() {
        StubBundle bundle = new StubBundle("org.eclipse.virgo.nano.startuptest", new Version(1,0,0));
        bundle.addHeader("Spring-Context", "testLocation");
        
        boolean isSpringDMPowered = SpringUtils.isSpringDMPoweredBundle(bundle);
        assertTrue(isSpringDMPowered);
    }

    @Test
    public void testIsBlueprintBundleSpringPoweredBundle() {
        StubBundle bundle = new StubBundle("org.eclipse.virgo.nano.startuptest", new Version(1,0,0));
        bundle.addHeader("Bundle-Blueprint", "testLocation");
        
        boolean isSpringDMPowered = SpringUtils.isSpringDMPoweredBundle(bundle);
        assertTrue(isSpringDMPowered);
    }
    
    @Test
    public void testIsBlueprintDisabledBundleNotSpringPoweredBundle() {
        StubBundle bundle = new StubBundle("org.eclipse.virgo.nano.startuptest", new Version(1,0,0));
        bundle.addHeader("Bundle-Blueprint", "");
        
        boolean isSpringDMPowered = SpringUtils.isSpringDMPoweredBundle(bundle);
        assertFalse(isSpringDMPowered);
    }
   
}
