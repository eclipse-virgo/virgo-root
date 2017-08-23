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

package org.eclipse.virgo.test.stubs.service.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import org.eclipse.virgo.test.stubs.service.event.internal.EventUtils;

/**
 * A stub implementation of {@link EventAdmin}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public class StubEventAdmin implements EventAdmin {

    private final List<Event> postedEvents = new ArrayList<Event>();

    private final List<Event> sentEvents = new ArrayList<Event>();

    private final Object monitor = new Object();

    /**
     * {@inheritDoc}
     */
    public void postEvent(Event event) {
        synchronized (this.monitor) {
            this.postedEvents.add(event);
            this.monitor.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendEvent(Event event) {
        synchronized (this.monitor) {
            this.sentEvents.add(event);
            this.monitor.notifyAll();
        }
    }

    /**
     * Waits for up to the supplied millisecond timeout period for the {@link EventAdmin#sendEvent(Event) sending} of an
     * {@link Event} that {@link Event#equals equals} the supplied <code>event</code>. Successfully waiting for an
     * <code>Event</code> removes it from the list of events such that a subsequent invocation would block until another
     * matching event was sent.
     * 
     * @param event The <code>Event</code> to await.
     * @param msTimeout The timeout period
     * @return <code>true</code> if the <code>Event</code> has been sent, otherwise <code>false</code>.
     */
    public boolean awaitSendingOfEvent(Event event, long msTimeout) {
        return awaitEvent(event, this.sentEvents, msTimeout);
    }

    /**
     * Waits for up to the supplied millisecond timeout period for the {@link EventAdmin#postEvent(Event) posting} of an
     * {@link Event} that {@link Event#equals equals} the supplied <code>event</code>. Successfully waiting for an
     * <code>Event</code> removes it from the list of events such that a subsequent invocation would block until another
     * matching event was posted.
     * 
     * @param event The <code>Event</code> to await.
     * @param msTimeout The timeout period
     * @return <code>true</code> if the <code>Event</code> has been posted, otherwise <code>false</code>.
     */
    public boolean awaitPostingOfEvent(Event event, long msTimeout) {
        return awaitEvent(event, this.postedEvents, msTimeout);
    }
    
    /**
     * Waits for up to the supplied millisecond timeout period for the {@link EventAdmin#sendEvent(Event) sending} of an
     * {@link Event} that has the supplied <code>topic</code>. Successfully waiting for an <code>Event</code> removes it
     * from the list of events such that a subsequent invocation would block until another matching event was sent.
     * 
     * @param topic The <code>topic</code> to await.
     * @param msTimeout The timeout period
     * @return the matching <code>Event</code>, or <code>null</code> if no <code>Event</code> is received.
     */
    public Event awaitSendingOfEvent(String topic, long msTimeout) {
        return awaitEventOnTopic(topic, this.sentEvents, msTimeout);
    }
    
    /**
     * Waits for up to the supplied millisecond timeout period for the {@link EventAdmin#postEvent(Event) posting} of an
     * {@link Event} that has the supplied <code>topic</code>. Successfully waiting for an <code>Event</code> removes it
     * from the list of events such that a subsequent invocation would block until another matching event was sent.
     * 
     * @param topic The <code>topic</code> to await.
     * @param msTimeout The timeout period
     * @return the matching <code>Event</code>, or <code>null</code> if no <code>Event</code> is received.
     */
    public Event awaitPostingOfEvent(String topic, long msTimeout) {
        return awaitEventOnTopic(topic, this.postedEvents, msTimeout);
    }

    private boolean awaitEvent(Event event, List<Event> eventList, long timeout) {
        long endTime = System.currentTimeMillis() + timeout;

        synchronized (this.monitor) {
            try {
                while (!removeEvent(event, eventList)) {
                    long waitTime = endTime - System.currentTimeMillis();

                    if (waitTime > 0) {
                        this.monitor.wait(waitTime);
                    } else {
                        return false;
                    }
                }
                return true;
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            return false;
        }
    }
    
    private Event awaitEventOnTopic(String topic, List<Event> eventList, long timeout) {
        long endTime = System.currentTimeMillis() + timeout;

        synchronized (this.monitor) {
            try {
                Event event = null;
                while (event == null) {
                    long waitTime = endTime - System.currentTimeMillis();

                    if (waitTime > 0) {
                        this.monitor.wait(waitTime);
                        event = removeEventOnTopic(topic, eventList);
                    } else {
                        break;
                    }
                }
                return event;
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            return null;
        }
    }

    private boolean removeEvent(Event event, List<Event> eventList) {
        
        Iterator<Event> candidates = eventList.iterator();
        
        while (candidates.hasNext()) {
            Event candidate = candidates.next();
            
            if (EventUtils.eventsAreEqual(candidate, event)) {
                candidates.remove();
                return true;
            }
        }
        
        return false;
    }
    
    private Event removeEventOnTopic(String topic, List<Event> eventList) {
        
        Iterator<Event> candidates = eventList.iterator();
        
        while (candidates.hasNext()) {
            Event candidate = candidates.next();
            
            if (topic.equals(candidate.getTopic())) {
                candidates.remove();
                return candidate;
            }            
        }
        
        return null;
    }
}
