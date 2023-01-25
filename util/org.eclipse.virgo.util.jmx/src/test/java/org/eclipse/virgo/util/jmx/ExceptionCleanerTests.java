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

package org.eclipse.virgo.util.jmx;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the Jmx {@link ExceptionCleaner} aspect which prevents undeclared exceptions leaking from {@link javax.management.MXBean @MXBean} method calls.
 */
public class ExceptionCleanerTests {

    @Test
    @Ignore
    public void testCaughtMethod() {
        ExceptionCleanerMXBean testBean = new ExceptionCleanerMXBean();

        try {
            testBean.caughtMethod();
            fail("Exception not thrown!");
        } catch (Exception e) {
            assertEquals("Does not throw a RuntimeException", RuntimeException.class, e.getClass());
            assertNotNull("Cause is null", e.getCause());
            assertEquals("Cause is not a RuntimeException", RuntimeException.class, e.getCause().getClass());
            assertEquals("java.lang.Exception: test exception", e.getCause().getMessage());
            assertEquals("Message not correctly generated.", "java.lang.RuntimeException: caughtMethod", e.getMessage());
            assertArrayEquals("Stack trace not passed through.", e.getStackTrace(), testBean.getStackTrace());
        }
    }

    @Test
    @Ignore
    public void testAnotherCaughtMethod() {
        ExceptionCleanerMXBean testBean = new ExceptionCleanerMXBean();

        try {
            testBean.anotherCaughtMethod();
            fail("Exception not thrown!");
        } catch (Exception e) {
            assertSame("Does not throw a RuntimeException", RuntimeException.class, e.getClass());
            assertNotNull("Cause is null", e.getCause());
            assertEquals("Cause is not a RuntimeException", RuntimeException.class, e.getCause().getClass());
            assertEquals("java.lang.Exception: test exception", e.getCause().getMessage());
            assertEquals("Message not correctly generated.", "java.lang.RuntimeException: anotherCaughtMethod", e.getMessage());
            assertArrayEquals("Stack trace not passed through.", e.getStackTrace(), testBean.getStackTrace());
        }
    }

    @Test
    // Note: None type annotations (from interfaces) aren't inherited
    public void testUncaughtMethod() {
        ExceptionCleanerNoMXBean testBean = new ExceptionCleanerNoMXBean();

        try {
            testBean.unCaughtMethod();
            fail("Exception not thrown!");
        } catch (Exception e) {
            assertSame("Does not throw a RuntimeException", e.getClass(), RuntimeException.class);
            assertNotNull("Cause is null.", e.getCause());
            assertSame("Cause not correct type.", e.getCause().getClass(), Exception.class);
            assertEquals("Message not correct.", "unCaughtMethod", e.getMessage());
            assertArrayEquals("Stack trace not passed through.", e.getStackTrace(), testBean.getStackTrace());
        }
    }

}
