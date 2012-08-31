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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.virgo.shell.Converter;
import org.eclipse.virgo.shell.internal.converters.LongConverter;
import org.junit.Test;


/**
 */
public class LongConverterTests {

    private final Long testLong = 45L;

    private final long testLongPrimitive = testLong.longValue();

    @Test
    public void testGetTypes() {
        String[] types = LongConverter.getTypes();
        List<String> typesList = Arrays.asList(types);
        assertTrue(typesList.contains(Long.class.getName()));
        assertTrue(typesList.contains(long.class.getName()));
    }

    @Test
    public void testConvert() throws Exception {
        LongConverter longConverter = new LongConverter();
        assertEquals(testLong, longConverter.convert(Long.class, testLong.toString()));
        assertEquals(testLong, longConverter.convert(long.class, testLong.toString()));
    }

    @Test
    public void testConvertBadInput() throws Exception {
        LongConverter longConverter = new LongConverter();
        assertNull(longConverter.convert(Integer.class, testLong.toString()));
        assertNull(longConverter.convert(int.class, testLong.toString()));
    }

    @Test
    public void testFormat() throws Exception {
        LongConverter longConverter = new LongConverter();
        assertEquals(testLong.toString(), longConverter.format(testLong, Converter.LINE, null));
        assertEquals(testLong.toString(), longConverter.format(testLongPrimitive, Converter.LINE, null));
    }

    @Test
    public void testFormatNotALong() throws Exception {
        LongConverter longConverter = new LongConverter();
        assertNull(longConverter.format(new Object(), Converter.LINE, null));
    }

}
