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

package org.eclipse.virgo.nano.core.internal.blueprint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

import org.eclipse.virgo.nano.core.internal.blueprint.ApplicationContextDependencyMonitor;
import org.eclipse.virgo.medic.test.eventlog.LoggedEvent;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.test.stubs.framework.StubBundle;

/**
 */
public class ApplicationContextDependencyMonitorTests {

    private final MockEventLogger eventLogger = new MockEventLogger();

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final ApplicationContextDependencyMonitor dependencyMonitor = new ApplicationContextDependencyMonitor(executor, eventLogger);

    private final StubBundle bundle = new StubBundle("the.bundle", new Version(1, 2, 3));

    @Test
    public void loggingOfWaitingEventForMandatoryService() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("filter", "theBean", true));
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(6000);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertEquals(1, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(0);

        assertEquals("KE0100W", loggedEvent.getCode());
        Object[] inserts = loggedEvent.getInserts();
        assertEquals(4, inserts.length);

        assertEquals("theBean", inserts[0]);
        assertEquals(this.bundle.getSymbolicName(), inserts[1]);
        assertEquals(this.bundle.getVersion(), inserts[2]);
        assertEquals("filter", inserts[3]);
    }

    @Test
    public void loggingOfWaitingEventForOptionalService() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("filter", "theBean", false));
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(6000);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertEquals(1, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(0);

        assertEquals("KE0100W", loggedEvent.getCode());
        Object[] inserts = loggedEvent.getInserts();
        assertEquals(4, inserts.length);

        assertEquals("theBean", inserts[0]);
        assertEquals(this.bundle.getSymbolicName(), inserts[1]);
        assertEquals(this.bundle.getVersion(), inserts[2]);
        assertEquals("filter", inserts[3]);
    }

    @Test
    public void loggingOfDependencySatisfied() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("filter", "theBean", true));
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(6000);

        event = new Event("org/osgi/service/blueprint/container/GRACE_PERIOD", createProperties());
        this.dependencyMonitor.handleEvent(event);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertEquals(2, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(1);

        assertEquals("KE0101I", loggedEvent.getCode());
        Object[] inserts = loggedEvent.getInserts();
        assertEquals(4, inserts.length);

        assertEquals("theBean", inserts[0]);
        assertEquals(this.bundle.getSymbolicName(), inserts[1]);
        assertEquals(this.bundle.getVersion(), inserts[2]);
        assertEquals("filter", inserts[3]);
    }

    @Test
    public void loggingOfMandatoryDependencyTimedOut() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("filter", "theBean", true));
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(6000);

        event = new Event("org/osgi/service/blueprint/container/FAILURE", createProperties("filter", "theBean", true));
        this.dependencyMonitor.handleEvent(event);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertEquals(2, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(1);

        assertEquals("KE0102E", loggedEvent.getCode());
        Object[] inserts = loggedEvent.getInserts();
        assertEquals(4, inserts.length);

        assertEquals("theBean", inserts[0]);
        assertEquals(this.bundle.getSymbolicName(), inserts[1]);
        assertEquals(this.bundle.getVersion(), inserts[2]);
        assertEquals("filter", inserts[3]);
    }

    @Test
    public void loggingOfOptionalDependencyTimedOut() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("filter", "theBean", false));
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(6000);

        event = new Event("org/osgi/service/blueprint/container/FAILURE", createProperties("filter", "theBean", false));
        this.dependencyMonitor.handleEvent(event);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertEquals(2, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(1);

        assertEquals("KE0102E", loggedEvent.getCode());
        Object[] inserts = loggedEvent.getInserts();
        assertEquals(4, inserts.length);

        assertEquals("theBean", inserts[0]);
        assertEquals(this.bundle.getSymbolicName(), inserts[1]);
        assertEquals(this.bundle.getVersion(), inserts[2]);
        assertEquals("filter", inserts[3]);
    }

    @Test
    public void containerCreationFailureRemovesTickers() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("filter", "theBean", true));
        this.dependencyMonitor.handleEvent(event);

        event = new Event("org/osgi/service/blueprint/container/FAILURE", createProperties());
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(3000);

        assertFalse(this.eventLogger.getCalled());
    }

    @Test
    public void containerCreationDrivesOutstandingTickers() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("filter", "theBean", true));
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(6000);

        event = new Event("org/osgi/service/blueprint/container/CREATED", createProperties());
        this.dependencyMonitor.handleEvent(event);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertEquals(2, loggedEvents.size());

        LoggedEvent loggedEvent = loggedEvents.get(1);

        assertEquals("KE0101I", loggedEvent.getCode());
        Object[] inserts = loggedEvent.getInserts();
        assertEquals(4, inserts.length);

        assertEquals("theBean", inserts[0]);
        assertEquals(this.bundle.getSymbolicName(), inserts[1]);
        assertEquals(this.bundle.getVersion(), inserts[2]);
        assertEquals("filter", inserts[3]);
    }

    @Test
    public void dependencyThatIsSatisfiedQuicklyLogsNothing() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("filter", "theBean", true));
        this.dependencyMonitor.handleEvent(event);

        event = new Event("org/osgi/service/blueprint/container/GRACE_PERIOD", createProperties());
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(3000);

        List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
        assertEquals(0, loggedEvents.size());
    }

    @Test
    public void slowServicesAreGivenLongerToBecomeAvailable() throws InterruptedException {
        Event event = new Event("org/osgi/service/blueprint/container/WAITING", createProperties("(org.eclipse.virgo.server.slowservice=true)",
            "theBean", true));
        this.dependencyMonitor.handleEvent(event);

        Thread.sleep(3000);

        assertFalse(this.eventLogger.getCalled());
    }

    @Test
    public void containerCreatedNotWaiting() {
        Event event = new Event("org/osgi/service/blueprint/container/CREATED", createProperties());
        this.dependencyMonitor.handleEvent(event);
    }

    @Test
    public void changeInUnsatisfiedDependenciesNotWaiting() {
        Event event = new Event("org/osgi/service/blueprint/container/GRACE_PERIOD", createProperties());
        this.dependencyMonitor.handleEvent(event);
    }

    @Test
    public void serviceDependenciesTimedOutNotWaiting() {
        Event event = new Event("org/osgi/service/blueprint/container/FAILURE", createProperties("filter", "theBean", false));
        this.dependencyMonitor.handleEvent(event);
    }

    @Test
    public void containerCreationFailedNotWaiting() {
        Event event = new Event("org/osgi/service/blueprint/container/FAILURE", createProperties());
        this.dependencyMonitor.handleEvent(event);
    }

    private Dictionary<String, Object> createProperties(String filter, String beanName, boolean mandatory) {
        Dictionary<String, Object> properties = createProperties();

        properties.put("dependencies", new String[] { filter });
        properties.put("bean.name", new String[] { beanName });
        properties.put("mandatory", new boolean[] { mandatory });

        return properties;
    }

    private Dictionary<String, Object> createProperties() {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(EventConstants.BUNDLE, this.bundle);
        return properties;
    }
}
