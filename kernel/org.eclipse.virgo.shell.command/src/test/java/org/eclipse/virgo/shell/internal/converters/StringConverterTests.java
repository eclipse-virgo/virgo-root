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

package org.eclipse.virgo.shell.internal.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.virgo.shell.internal.converters.StringConverter;
import org.junit.Test;



/**
 * <p>
 * StringConverterTests for the basic function of {@link StringConverter} the format method is not used.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Test class
 *
 */
public class StringConverterTests {

    private static final String testString = "formattedString";

    /**
     * Test method for {@link StringConverter#getTypes()}.
     */
    @Test
    public void testGetTypes() {
        assertEquals(String.class.getName(), StringConverter.getTypes());
    }

    /**
     * Test method for {@link StringConverter#convert(java.lang.Class, java.lang.Object)}.
     * @throws Exception 
     */
    @Test
    public void testConvert() throws Exception {
        StringConverter stringConverter = new StringConverter();
        assertEquals(testString, stringConverter.convert(String.class, testString));
    }

    /**
     * Test method for {@link StringConverter#convert(java.lang.Class, java.lang.Object)}.
     * @throws Exception 
     */
    @Test
    public void testConvertBadType() throws Exception {
        StringConverter stringConverter = new StringConverter();
        assertEquals(null, stringConverter.convert(Integer.class, testString));
    }

    /**
     * Test method for {@link StringConverter#format(java.lang.Object, int, org.eclipse.virgo.shell.Converter)}.
     * @throws Exception 
     */
    @Test
    public void testFormat() throws Exception {
        StringConverter stringConverter = new StringConverter();
        assertEquals(testString, stringConverter.format(testString, 0, null));
    }

    /**
     * Test method for {@link StringConverter#format(java.lang.Object, int, org.eclipse.virgo.shell.Converter)}.
     * @throws Exception 
     */
    @Test
    public void testFormatNotAString() throws Exception {
        StringConverter stringConverter = new StringConverter();
        assertNull(stringConverter.format(new Object(), 0, null));
    }

}
