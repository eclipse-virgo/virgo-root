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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.virgo.util.jmx.ExceptionCleaner;
import org.junit.Test;

/**
 * Test the Jmx {@link ExceptionCleaner} aspect which prevents undeclared exceptions leaking from {@link javax.management.MXBean @MXBean} method calls.
 * 
 */
public class ExceptionCleanerTests {

    
    public class ExceptionCleanerMXBean implements JmxExceptionCleanerExtensionTestInterface {

        private StackTraceElement[] steArray = null;

        public void unCaughtMethod() throws Exception {
            RuntimeException rte = new RuntimeException("unCaughtMethod", new Exception("test exception"));
            this.steArray = rte.getStackTrace();
            throw rte;
        }

        public void caughtMethod() throws Exception {
            RuntimeException rte = new RuntimeException("caughtMethod", new Exception("test exception"));
            this.steArray = rte.getStackTrace();
            throw rte;
        }
        
        public StackTraceElement[] getStackTrace() {
            return this.steArray;
        }

        public void anotherCaughtMethod() {
            RuntimeException rte = new RuntimeException("anotherCaughtMethod", new Exception("test exception"));
            this.steArray = rte.getStackTrace();
            throw rte;            
        }

    }

    @Test
    public void testCaughtMethod() throws Exception {
        ExceptionCleanerMXBean testBean = new ExceptionCleanerMXBean();
        
        try {
            testBean.caughtMethod();
            assertTrue("Exception not thrown!", false);
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
    public void testAnotherCaughtMethod() throws Exception {
        ExceptionCleanerMXBean testBean = new ExceptionCleanerMXBean();
        
        try {
            testBean.anotherCaughtMethod();
            assertTrue("Exception not thrown!", false);
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
    public void testUncaughtMethod() throws Exception {
        ExceptionCleanerMXBean testBean = new ExceptionCleanerMXBean();
        
        try {
            testBean.unCaughtMethod();
            assertTrue("Exception not thrown!", false);
        } catch (Exception e) {
            assertSame("Does not throw a RuntimeException", e.getClass(), RuntimeException.class);
            assertNotNull("Cause is null.", e.getCause());
            assertSame("Cause not correct type.", e.getCause().getClass(), Exception.class);
            assertEquals("Message not correct.", "unCaughtMethod", e.getMessage());
            assertArrayEquals("Stack trace not passed through.", e.getStackTrace(), testBean.getStackTrace());
        }
    }

}
