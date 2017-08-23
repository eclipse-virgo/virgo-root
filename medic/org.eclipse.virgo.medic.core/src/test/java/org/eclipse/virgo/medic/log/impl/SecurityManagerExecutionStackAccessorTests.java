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

package org.eclipse.virgo.medic.log.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.medic.log.impl.ExecutionStackAccessor;
import org.eclipse.virgo.medic.log.impl.SecurityManagerExecutionStackAccessor;
import org.junit.Test;

public class SecurityManagerExecutionStackAccessorTests {

    @Test
    public void getClasses() {
        ExecutionStackAccessor accessor = new SecurityManagerExecutionStackAccessor();
        Class<?>[] classes = accessor.getExecutionStack();
        assertNotNull(classes);
        assertTrue(classes.length > 0);
        assertEquals("Unexpected class in stack: " + classes[0], SecurityManagerExecutionStackAccessorTests.class, classes[0]);
    }
}
