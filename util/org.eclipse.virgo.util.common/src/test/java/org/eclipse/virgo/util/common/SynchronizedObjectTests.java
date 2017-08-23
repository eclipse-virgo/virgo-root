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

package org.eclipse.virgo.util.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.virgo.util.common.SynchronizedObject;
import org.junit.Test;

/**
 */
public class SynchronizedObjectTests {

    private static final String STRING = "a";
    
    private final Object monitor = new Object();

    @Test
    public void testToString() {
        assertEquals(STRING, (new SynchronizedObject(STRING, this.monitor)).toString());
    }
    
    @Test
    public void testEquals() {
        assertFalse(STRING.equals(new SynchronizedObject(STRING, this.monitor)));
    }
    
}
