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

package org.eclipse.virgo.apps.admin.web.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.virgo.apps.admin.web.internal.StandardDumpListFormatterUtil;
import org.eclipse.virgo.apps.admin.web.stubs.StubDumpInspectorService;
import org.junit.Test;



/**
 */
public class StandardDumpListFormatterUtilTests {

    private static final String FOO_STAMP = "foo";
    
    private static final String TIME_STAMP = "2009110516301643565467564764";
    
    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.web.internal.StandardDumpListFormatterUtil#getAvaliableDumps()}.
     */
    @Test
    public void testGetAvaliableDumps() {
        StandardDumpListFormatterUtil standardDumpListFormatterUtil = new StandardDumpListFormatterUtil(new StubDumpInspectorService(FOO_STAMP, TIME_STAMP));
        Map<String, String> avaliableDumps = standardDumpListFormatterUtil.getAvaliableDumps();
        assertNotNull(avaliableDumps);
        assertTrue(avaliableDumps.containsKey(FOO_STAMP));
        assertEquals(FOO_STAMP, avaliableDumps.get(FOO_STAMP));
        assertTrue(avaliableDumps.containsKey(TIME_STAMP));
        assertTrue("2009110516301643565467564764".equals(avaliableDumps.get(TIME_STAMP)));
    }

}

