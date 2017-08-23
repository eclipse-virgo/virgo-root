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

package org.eclipse.virgo.medic.log.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.virgo.medic.log.appender.StubAppender;
import org.junit.BeforeClass;
import org.junit.Test;

import test.TestClass;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;


public class EntryExitTraceTests {

    private final TestClass testClass = new TestClass();

    @BeforeClass
    public static void ensureConfiguredContextSelectorIsDefaultContextSelector() {
        System.setProperty("logback.ContextSelector", "ch.qos.logback.classic.selector.DefaultContextSelector");
    }

    @Test
    public void testPublicBefore() {
        testClass.publicTest(false);
        List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
        assertEquals(2, loggingEvents.size());
        LoggingEvent event = loggingEvents.get(0);
        assertEquals(Level.DEBUG, event.getLevel());
        assertEquals("> public void test.TestClass.publicTest(boolean)", event.getFormattedMessage());
        assertEquals("public void test.TestClass.publicTest(boolean)", event.getArgumentArray()[1]);
    }

    @Test
    public void testPublicAfterReturn() {
        testClass.publicTest(false);
        List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
        assertEquals(2, loggingEvents.size());
        LoggingEvent event = loggingEvents.get(1);
        assertEquals(Level.DEBUG, event.getLevel());
        assertEquals("< public void test.TestClass.publicTest(boolean)", event.getFormattedMessage());
        assertEquals("public void test.TestClass.publicTest(boolean)", event.getArgumentArray()[1]);
    }

    @Test
    public void testPublicAfterThrowing() {
        try {
            testClass.publicTest(true);
        } catch (Exception e) {
            List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
            assertEquals(2, loggingEvents.size());
            LoggingEvent event = loggingEvents.get(1);
            assertEquals(Level.DEBUG, event.getLevel());
            assertEquals("< public void test.TestClass.publicTest(boolean)", event.getFormattedMessage());
            assertEquals(RuntimeException.class.getName(), event.getThrowableProxy().getClassName());
        }
    }

    @Test
    public void testPackagePrivateBefore() throws Exception {
        Method method = testClass.getClass().getDeclaredMethod("packagePrivateTest", boolean.class);
        method.setAccessible(true);
        method.invoke(testClass, false);
        List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
        assertEquals(2, loggingEvents.size());
        LoggingEvent event = loggingEvents.get(0);
        assertEquals(Level.TRACE, event.getLevel());
        assertEquals("> void test.TestClass.packagePrivateTest(boolean)", event.getFormattedMessage());
        assertEquals("void test.TestClass.packagePrivateTest(boolean)", event.getArgumentArray()[1]);
    }

    @Test
    public void testPackagePrivateAfterReturn() throws Exception {
        Method method = testClass.getClass().getDeclaredMethod("packagePrivateTest", boolean.class);
        method.setAccessible(true);
        method.invoke(testClass, false);
        List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
        assertEquals(2, loggingEvents.size());
        LoggingEvent event = loggingEvents.get(1);
        assertEquals(Level.TRACE, event.getLevel());
        assertEquals("< void test.TestClass.packagePrivateTest(boolean)", event.getFormattedMessage());
        assertEquals("void test.TestClass.packagePrivateTest(boolean)", event.getArgumentArray()[1]);
    }

    @Test
    public void testPackagePrivateAfterThrowing() {
        try {
            Method method = testClass.getClass().getDeclaredMethod("packagePrivateTest", boolean.class);
            method.setAccessible(true);
            method.invoke(testClass, true);
        } catch (Exception e) {
            List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
            assertEquals(2, loggingEvents.size());
            LoggingEvent event = loggingEvents.get(1);
            assertEquals(Level.TRACE, event.getLevel());
            assertEquals("< void test.TestClass.packagePrivateTest(boolean)", event.getFormattedMessage());
            assertEquals(RuntimeException.class.getName(), event.getThrowableProxy().getClassName());
        }
    }

    @Test
    public void testPrivateBefore() throws Exception {
        Method method = testClass.getClass().getDeclaredMethod("privateTest", boolean.class);
        method.setAccessible(true);
        method.invoke(testClass, false);
        List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
        assertEquals(2, loggingEvents.size());
        LoggingEvent event = loggingEvents.get(0);
        assertEquals(Level.TRACE, event.getLevel());
        assertEquals("> private void test.TestClass.privateTest(boolean)", event.getFormattedMessage());
        assertEquals("private void test.TestClass.privateTest(boolean)", event.getArgumentArray()[1]);
    }

    @Test
    public void testPrivateAfterReturn() throws Exception {
        Method method = testClass.getClass().getDeclaredMethod("privateTest", boolean.class);
        method.setAccessible(true);
        method.invoke(testClass, false);
        List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
        assertEquals(2, loggingEvents.size());
        LoggingEvent event = loggingEvents.get(1);
        assertEquals(Level.TRACE, event.getLevel());
        assertEquals("< private void test.TestClass.privateTest(boolean)", event.getFormattedMessage());
        assertEquals("private void test.TestClass.privateTest(boolean)", event.getArgumentArray()[1]);
    }

    @Test
    public void testPrivateAfterThrowing() {
        try {
            Method method = testClass.getClass().getDeclaredMethod("privateTest", boolean.class);
            method.setAccessible(true);
            method.invoke(testClass, true);
        } catch (Exception e) {
            List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents(null);
            assertEquals(2, loggingEvents.size());
            LoggingEvent event = loggingEvents.get(1);
            assertEquals(Level.TRACE, event.getLevel());
            assertEquals("< private void test.TestClass.privateTest(boolean)", event.getFormattedMessage());
            assertEquals(RuntimeException.class.getName(), event.getThrowableProxy().getClassName());
        }
    }
}
