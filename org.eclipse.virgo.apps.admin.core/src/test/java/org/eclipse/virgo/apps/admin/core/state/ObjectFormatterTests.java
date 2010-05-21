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

package org.eclipse.virgo.apps.admin.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.apps.admin.core.state.ObjectFormatter;
import org.junit.Test;


/**
 */
public class ObjectFormatterTests {

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.ObjectFormatter#formatObject(java.lang.Object)}.
     */
    @Test
    public void testFormatObject() {
        Object[] fooArray = new Object[3];
        fooArray[0] = "a";
        fooArray[1] = "b";
        fooArray[2] = "c";
        String formatObject = ObjectFormatter.formatObject(fooArray);
        assertTrue(formatObject.contains("a"));
        assertTrue(formatObject.contains("b"));
        assertTrue(formatObject.contains("c"));
        assertTrue(formatObject.contains(", "));
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.ObjectFormatter#formatMapValues(java.util.Map)}.
     */
    @Test
    public void testFormatMapValues() {
        Map<String, Object> properties= new HashMap<String, Object>();
        
        properties.put("a", 56);
        
        Object[] fooArray = new Object[3];
        fooArray[0] = "a";
        fooArray[1] = "b";
        fooArray[2] = "c";
        properties.put("b", fooArray);
        
        Map<String, String> formatMapValues = ObjectFormatter.formatMapValues(properties);
        
        assertEquals(2, formatMapValues.size());
        assertEquals("56", formatMapValues.get("a"));
        
        assertTrue(formatMapValues.get("b").contains("a"));
        assertTrue(formatMapValues.get("b").contains("b"));
        assertTrue(formatMapValues.get("b").contains("c"));
        assertTrue(formatMapValues.get("b").contains(", "));
    }

}
