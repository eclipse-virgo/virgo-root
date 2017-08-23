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

package org.eclipse.virgo.medic.test.eventlog;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.test.eventlog.LoggedEvent;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.junit.Test;


public class MockEventLoggerTests {

    private final MockEventLogger eventLogger = new MockEventLogger();

    @Test
    public void emptyList() {
        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertNotNull(loggedEvents);
        assertEquals(0, loggedEvents.size());
    }

    @Test
    public void eventWithoutThrowableWithInserts() {
        this.eventLogger.log("123", Level.INFO, "a", "b", true);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertNotNull(loggedEvents);
        assertEquals(1, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(0);
        assertEquals("123", loggedEvent.getCode());
        assertEquals(Level.INFO, loggedEvent.getLevel());
        assertNull(loggedEvent.getThrowable());
        assertArrayEquals(new Object[] { "a", "b", true }, loggedEvent.getInserts());
    }

    @Test
    public void eventWithoutThrowableWithoutInserts() {
        this.eventLogger.log("234", Level.ERROR);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertNotNull(loggedEvents);
        assertEquals(1, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(0);
        assertEquals("234", loggedEvent.getCode());
        assertEquals(Level.ERROR, loggedEvent.getLevel());
        assertNull(loggedEvent.getThrowable());
        assertNotNull(loggedEvent.getInserts());
        assertEquals(0, loggedEvent.getInserts().length);
    }

    @Test
    public void eventWithThrowableWithInserts() {
        Throwable throwable = new Throwable();
        this.eventLogger.log("345", Level.WARNING, throwable, "a", "b", true);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertNotNull(loggedEvents);
        assertEquals(1, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(0);
        assertEquals("345", loggedEvent.getCode());
        assertEquals(Level.WARNING, loggedEvent.getLevel());
        assertEquals(throwable, loggedEvent.getThrowable());
        assertArrayEquals(new Object[] { "a", "b", true }, loggedEvent.getInserts());
    }

    @Test
    public void eventWithThrowableWithoutInserts() {
        Throwable throwable = new Throwable();
        this.eventLogger.log("456", Level.ERROR, throwable);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertNotNull(loggedEvents);
        assertEquals(1, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(0);
        assertEquals("456", loggedEvent.getCode());
        assertEquals(Level.ERROR, loggedEvent.getLevel());
        assertEquals(throwable, loggedEvent.getThrowable());
        assertNotNull(loggedEvent.getInserts());
        assertEquals(0, loggedEvent.getInserts().length);
    }

    @Test
    public void multipleEventsIncludingDuplicates() {
        this.eventLogger.log("123", Level.INFO);
        this.eventLogger.log("234", Level.INFO);
        this.eventLogger.log("345", Level.INFO);
        this.eventLogger.log("345", Level.INFO);
        this.eventLogger.log("123", Level.INFO);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertNotNull(loggedEvents);
        assertEquals(5, loggedEvents.size());
        assertLoggedEventMatch(loggedEvents.get(0), "123", null, null);
        assertLoggedEventMatch(loggedEvents.get(1), "234", null, null);
        assertLoggedEventMatch(loggedEvents.get(2), "345", null, null);
        assertLoggedEventMatch(loggedEvents.get(3), "345", null, null);
        assertLoggedEventMatch(loggedEvents.get(4), "123", null, null);
        assertTrue("Correct event code sequence not detected", this.eventLogger.isLogged("123", "234", "345", "345", "123"));
        assertFalse("Incorrect event code sequence not detected", this.eventLogger.isLogged("123", "234", "345", "123"));
        assertFalse("Incorrect event code sequence not detected", this.eventLogger.isLogged("123", "234", "345", "123", "123"));
    }

    @Test
    public void retrieveEventSequenceWithCodes() {
        this.eventLogger.log("123", Level.INFO, new Integer(1));
        this.eventLogger.log("234", Level.INFO, new Integer(2));
        this.eventLogger.log("345", Level.INFO, new Integer(3));
        this.eventLogger.log("345", Level.INFO, new Integer(4));
        this.eventLogger.log("123", Level.INFO, new Integer(5));

        List<LoggedEvent> loggedEvents = this.eventLogger.getEventsWithCodes("345", "123", "345", "xyz");
        assertNotNull(loggedEvents);
        assertEquals(2, loggedEvents.size());
        assertLoggedEventMatch(loggedEvents.get(0), "345", Level.INFO, null, 3);
        assertLoggedEventMatch(loggedEvents.get(1), "123", Level.INFO, null, 5);
    }

    @Test
    public void containsEventCodeSequence() {
        this.eventLogger.log("123", Level.INFO);
        this.eventLogger.log("234", Level.INFO);
        this.eventLogger.log("345", Level.INFO);
        this.eventLogger.log("345", Level.INFO);
        this.eventLogger.log("123", Level.INFO);

        assertTrue("Failed to detect codes 234, 123, 345", this.eventLogger.containsLogged("234","123","345"));
        assertFalse("Erroneously detected all codes 123, xyz, 234", this.eventLogger.containsLogged("123", "xyz", "234"));
        this.eventLogger.reinitialise();
        assertFalse("Detected code 123 after reinitialisation", this.eventLogger.containsLogged("123"));
        assertTrue("eventLogger not empty after reinitialisation", this.eventLogger.getLoggedEvents().isEmpty());
    }

    private static void assertLoggedEventMatch(LoggedEvent loggedEvent, String code, Level level, Throwable throwable, Object... inserts) {
        assertNotNull(loggedEvent);
        assertEquals("LoggedEvent code", code, loggedEvent.getCode());
        if (level != null) {
            assertEquals("LoggedEvent level in " + code, level, loggedEvent.getLevel());
        }
        assertEquals("LoggedEvent throwable in " + code, throwable, loggedEvent.getThrowable());
        assertEquals("Number of inserts in " + code, inserts.length, loggedEvent.getInserts().length);
        int index = 0;
        for (Object insert : inserts) {
            assertEquals("LoggedEvent insert in " + code, insert, loggedEvent.getInserts()[index++]);
        }
    }
}
