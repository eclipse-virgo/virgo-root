/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.util.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test {@link FatalIOException} basic minimum needs.
 * 
 * @author Steve Powell
 */
public class FatalIOExceptionTests {

    @Test
    public void testFataIOExceptionType() {
        FatalIOException fioe = new FatalIOException("test");
        assertTrue("FatalIOException is not a RuntimeException - it must be unchecked.", RuntimeException.class.isAssignableFrom(fioe.getClass()));
    }

    @Test
    public void testFataIOExceptionState() {
        Exception cause = new Exception("cause");
        FatalIOException fioe = new FatalIOException("test", cause);
        assertEquals("Cause not preserved.", cause, fioe.getCause());
        assertEquals("Message not preserved.", "test", fioe.getMessage());
    }

}
