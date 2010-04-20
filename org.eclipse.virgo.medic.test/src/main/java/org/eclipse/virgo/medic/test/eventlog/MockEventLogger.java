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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;


/**
 * A mock implementation of {@link EventLogger} for testing purposes. The events that have been logged can be accessed
 * by calling {@link #getLoggedEvents()}.<br/>
 * This implementation saves all events for subsequent analysis, and has an option to clear the log for re-use.<br/>
 * This version also has an operation for asserting that the sequence of events matches a supplied array. This is for
 * convenient unit tests.
 * <p/>
 * 
 * <strong>Concurrent Semantics:</strong><br/>
 * Thread-safe during logging, but not for retrieval and re-initialisation.
 */
public class MockEventLogger implements EventLogger {

    private final List<LoggedEvent> loggedEvents = new CopyOnWriteArrayList<LoggedEvent>();

    private final boolean noisy;

    private volatile boolean called = false;

    private final PrintStream printStream;
    
    public MockEventLogger() {
        this(false, null);
    }

    public MockEventLogger(boolean noisy, PrintStream printStream) {
        this.noisy = noisy;
        this.printStream = printStream;
    }

    public void log(LogEvent logEvent, Object... inserts) {
        this.log(logEvent.getEventCode(), logEvent.getLevel(), inserts);
    }

    public void log(LogEvent logEvent, Throwable throwable, Object... inserts) {
        this.log(logEvent.getEventCode(), logEvent.getLevel(), throwable, inserts);
    }

    public void log(String code, Level level, Object... inserts) {
        LoggedEvent loggedEvent = new LoggedEvent(code, level, inserts);
        this.loggedEvents.add(loggedEvent);
        this.called = true;
        if (noisy) {
            loggedEvent.print(this.printStream);
        }
    }

    public void log(String code, Level level, Throwable throwable, Object... inserts) {
        LoggedEvent loggedEvent = new LoggedEvent(code, level, throwable, inserts);
        this.loggedEvents.add(loggedEvent);
        this.called = true;
        if (noisy) {
            loggedEvent.print(this.printStream);
        }
    }

    /**
     * Returns a <code>List</code> of the events that have been logged. The order of the items in the list corresponds
     * to the order in which the events were logged. If no events have been logged an empty list is returned.
     * 
     * @return the logged events
     */
    public List<LoggedEvent> getLoggedEvents() {
        return this.loggedEvents;
    }

    /**
     * @return true if this EventLogger has been called since (re)initialisation.
     */
    public boolean getCalled() {
        return this.called;
    }

    /**
     * Reinitialise this EventLogger.
     */
    public void reinitialise() {
        this.called = false;
        this.loggedEvents.clear();
    }

    /**
     * Helper method for unit tests to check sequence of logged events after a test.
     * 
     * @param codes sequence of message codes expected
     * @return true iff sequence of codes matches logged events exactly
     */
    public boolean isLogged(String... codes) {
        if (this.loggedEvents.size() != codes.length) {
            return false;
        }
        int index = 0;
        for (String code : codes) {
            if (!code.equals(this.loggedEvents.get(index++).getCode())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method for unit tests to check superset of logged events after a test.
     * 
     * @param codes sequence of message codes expected to be contained in logged events (in any order)
     * @return true iff the codes are all in the saved {@link LoggedEvent}s.
     */
    public boolean containsLogged(String... codes) {
        List<String> codeList = new ArrayList<String>();
        for (LoggedEvent event : this.loggedEvents) {
            codeList.add(event.getCode());
        }
        for (String code : codes) {
            if (!codeList.contains(code)){
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method for unit tests to retrieve specific logged events from saved events.
     * 
     * @param codes sequence of message codes to have events extracted, in order.
     * @return LoggedEvents with supplied codes in same sequence from log.
     */
    public List<LoggedEvent> getEventsWithCodes(String... codes) {
        List<LoggedEvent> eventList = new ArrayList<LoggedEvent>();
        int eventIndex = 0;
        int loggedEventsSize = this.loggedEvents.size();
        for (String code : codes) {
            while (eventIndex < loggedEventsSize) {
                LoggedEvent loggedEvent = this.loggedEvents.get(eventIndex++);
                if (code.equals(loggedEvent.getCode())) {
                    eventList.add(loggedEvent);
                    break;
                }
            }
        }
        return eventList;
    }
}
