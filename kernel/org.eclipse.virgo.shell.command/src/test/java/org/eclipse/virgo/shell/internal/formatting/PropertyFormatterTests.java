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

package org.eclipse.virgo.shell.internal.formatting;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.virgo.shell.internal.formatting.PropertyFormatter;
import org.junit.Test;



/**
 */
public class PropertyFormatterTests {
    
    @Test
    public void formatSingleEntry() {
        List<String> formatted = PropertyFormatter.formatPropertyValue("a single entry", 80);
        assertEquals(1, formatted.size());
        assertEquals("a single entry", formatted.get(0));
        
        formatted = PropertyFormatter.formatPropertyValue("a single entry", 8);
        assertEquals(1, formatted.size());
        assertEquals("a single entry", formatted.get(0));
    }
    
    @Test
    public void formatArrayEntry() {
        List<String> formatted = PropertyFormatter.formatPropertyValue(new String[] {"alpha", "bravo", "charlie", "delta", "echo"}, 20);
        assertEquals(2, formatted.size());
        assertEquals("alpha, bravo, ", formatted.get(0));
        assertEquals("charlie, delta, echo", formatted.get(1));        
        
        formatted = PropertyFormatter.formatPropertyValue(new String[] {"alpha", "bravo", "charlie", "delta", "echo"}, 5);
        assertEquals(5, formatted.size());
        assertEquals("alpha, ", formatted.get(0));
        assertEquals("bravo, ", formatted.get(1));
        assertEquals("charlie, ", formatted.get(2));
        assertEquals("delta, ", formatted.get(3));
        assertEquals("echo", formatted.get(4));
    }
}
