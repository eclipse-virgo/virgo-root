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

import static org.junit.Assert.*;

import org.eclipse.virgo.apps.admin.web.internal.AdminConsoleUtil;
import org.junit.Test;

/**
 * Unit tests for {@link AdminConsoleUtil}.
 * 
 */
public class AdminConsoleUtilTests {

    @Test
    public void testServerPropertiesPresent() {
        AdminConsoleUtil service = new AdminConsoleUtil();
    	
        assertNotNull(service.getJavaDesc());
        assertNotNull(service.getOperatingSystem());
        assertNotNull(service.getServerVersion());
        assertNotNull(service.getVMDesc());
        assertNotNull(service.getUserTimeZone());

        assertTrue(service.getJavaDesc().length() != 0);
        assertTrue(service.getOperatingSystem().length() != 0);
        assertTrue(service.getServerVersion().length() != 0);
        assertTrue(service.getVMDesc().length() != 0);
        assertTrue(service.getUserTimeZone().length() != 0);
        
        assertEquals("test.version", service.getServerVersion());
    }

}

