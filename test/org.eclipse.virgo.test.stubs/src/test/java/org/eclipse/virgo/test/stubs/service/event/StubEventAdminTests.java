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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.osgi.service.event.Event;


public class StubEventAdminTests {

    private final StubEventAdmin eventAdmin = new StubEventAdmin();
    
    private final Dictionary<String, ?> properties = createProperties();
    
    private final Dictionary<String, ?> expectedProperties = createProperties();
    
    private Dictionary<String, ?> createProperties() {
        Dictionary<String, Object> properties = new Hashtable<>();
        
        properties.put("booleanArray", new boolean[] {false, true});
        properties.put("byteArray", new byte[] {1, 2});
        properties.put("charArray", new char[] {'a', 'b'});
        properties.put("doubleArray", new double[] {1.0d});
        properties.put("floatArray", new float[] {2.45f});
        properties.put("intArray", new int[] {1, 2, 3});
        properties.put("longArray", new long[] {1L, 2L});
        properties.put("shortArray", new short[] {5, 9, 18});
        
        return properties;
    }

    @Test
    public void postEvent() {
        Event posted = new Event("topic", this.properties);
        Event expected = new Event("topic", this.expectedProperties);

        this.eventAdmin.postEvent(posted);
        assertTrue(this.eventAdmin.awaitPostingOfEvent(expected, 1000));
    }

    @Test
    public void sendEvent() {
        Event sent = new Event("topic", this.properties);
        Event expected = new Event("topic", this.expectedProperties);

        this.eventAdmin.sendEvent(sent);
        assertTrue(this.eventAdmin.awaitSendingOfEvent(expected, 1000));
    }

    @Test
    public void awaitSendingTimeout() {
        Event expected = new Event("topic", this.expectedProperties);
        
        this.eventAdmin.sendEvent(new Event("differentTopic", (Map<String,?>)null));
        this.eventAdmin.postEvent(expected);
        
        long start = System.currentTimeMillis();
        assertFalse(this.eventAdmin.awaitSendingOfEvent(expected, 500));
        long delta = System.currentTimeMillis() - start;
        assertTrue("Delta of " + delta + " was less than expected", delta >= 500);
    }

    @Test
    public void awaitPostingTimeout() {
        Event expected = new Event("topic", this.expectedProperties);
        
        this.eventAdmin.postEvent(new Event("differentTopic", (Map<String,?>)null));
        this.eventAdmin.sendEvent(expected);
        
        long start = System.currentTimeMillis();
        assertFalse(this.eventAdmin.awaitPostingOfEvent(expected, 500));
        long delta = System.currentTimeMillis() - start;
        assertTrue("Delta of " + delta + " was less than expected", delta >= 500);
    }

    @Test
    public void postingOfEventWhileWaiting() throws InterruptedException {
        Event posted = new Event("topic", this.properties);
        final Event expected = new Event("topic", this.expectedProperties);

        final CountDownLatch latch = new CountDownLatch(1);

        Thread awaitingThread = new Thread(() -> {
            if (eventAdmin.awaitPostingOfEvent(expected, 10000)) {
                latch.countDown();
            }
        });

        awaitingThread.start();

        this.eventAdmin.postEvent(posted);

        assertTrue(latch.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void sendingOfEventWhileWaiting() throws InterruptedException {
        Event posted = new Event("topic", this.properties);
        final Event expected = new Event("topic", this.expectedProperties);

        final CountDownLatch latch = new CountDownLatch(1);

        Thread awaitingThread = new Thread(() -> {
            if (eventAdmin.awaitSendingOfEvent(expected, 10000)) {
                latch.countDown();
            }
        });

        awaitingThread.start();

        this.eventAdmin.sendEvent(posted);

        assertTrue(latch.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void removalOfPostedEventWhenSuccessfullyAwaited() {
        Event posted = new Event("topic", this.properties);
        Event expected = new Event("topic", this.expectedProperties);

        this.eventAdmin.postEvent(posted);
        assertTrue(this.eventAdmin.awaitPostingOfEvent(expected, 1000));
        assertFalse(this.eventAdmin.awaitPostingOfEvent(expected, 1));
    }

    @Test
    public void removalOfSentEventWhenSuccessfullyAwaited() {
        Event sent = new Event("topic", this.properties);
        Event expected = new Event("topic", this.expectedProperties);

        this.eventAdmin.sendEvent(sent);
        assertTrue(this.eventAdmin.awaitSendingOfEvent(expected, 1000));
        assertFalse(this.eventAdmin.awaitSendingOfEvent(expected, 1));
    }

    @Test(timeout = 30000)
    public void awaitingThreadsCanBeInterrupted() throws InterruptedException {
        final Event expected = new Event("topic", this.expectedProperties);

        final CountDownLatch latch = new CountDownLatch(1);

        Thread awaitingThread = new Thread(() -> {
            eventAdmin.awaitSendingOfEvent(expected, Long.MAX_VALUE);
            latch.countDown();
        });

        awaitingThread.start();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        ThreadInfo threadInfo;

        while ((threadInfo = threadBean.getThreadInfo(awaitingThread.getId())) == null || threadInfo.getThreadState() != State.TIMED_WAITING) {
            Thread.sleep(10);
        }

        awaitingThread.interrupt();

        latch.await();
    }
    
    @Test
    public void postEventMatchingOnTopic() {
        Event posted = new Event("topic", this.properties);

        this.eventAdmin.postEvent(posted);
        assertNotNull(this.eventAdmin.awaitPostingOfEvent("topic", 1000));
    }

    @Test
    public void sendEventMatchingOnTopic() {
        Event sent = new Event("topic", this.properties);

        this.eventAdmin.sendEvent(sent);
        assertNotNull(this.eventAdmin.awaitSendingOfEvent("topic", 1000));
    }

    @Test
    public void awaitSendingTimeoutMatchingOnTopic() {
        Event expected = new Event("topic", this.expectedProperties);
        
        this.eventAdmin.sendEvent(new Event("differentTopic", (Map<String,?>)null));
        this.eventAdmin.postEvent(expected);
        
        long start = System.currentTimeMillis();
        assertNull(this.eventAdmin.awaitSendingOfEvent("topic", 500));
        long delta = System.currentTimeMillis() - start;
        assertTrue("Delta of " + delta + " was less than expected", delta >= 500);
    }

    @Test
    public void awaitPostingTimeoutMatchingOnTopic() {
        Event expected = new Event("topic", this.expectedProperties);
        
        this.eventAdmin.postEvent(new Event("differentTopic", (Map<String,?>)null));
        this.eventAdmin.sendEvent(expected);
        
        long start = System.currentTimeMillis();
        assertNull(this.eventAdmin.awaitPostingOfEvent("topic", 500));
        long delta = System.currentTimeMillis() - start;
        assertTrue("Delta of " + delta + " was less than expected", delta >= 500);
    }

    @Test
    public void postingOfEventWhileWaitingMatchingOnTopic() throws InterruptedException {
        Event posted = new Event("topic", this.properties);

        final CountDownLatch latch = new CountDownLatch(1);

        Thread awaitingThread = new Thread(() -> {
            if (eventAdmin.awaitPostingOfEvent("topic", 1000) != null) {
                latch.countDown();
            }
        });

        awaitingThread.start();

        this.eventAdmin.postEvent(posted);

        assertTrue(latch.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void sendingOfEventWhileWaitingMatchingOnTopic() throws InterruptedException {
        Event posted = new Event("topic", this.properties);

        final CountDownLatch latch = new CountDownLatch(1);

        Thread awaitingThread = new Thread(() -> {
            if (eventAdmin.awaitSendingOfEvent("topic", 1000) != null) {
                latch.countDown();
            }
        });

        awaitingThread.start();

        this.eventAdmin.sendEvent(posted);

        assertTrue(latch.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void removalOfPostedEventWhenSuccessfullyAwaitedMatchingOnTopic() {
        Event posted = new Event("topic", this.properties);

        this.eventAdmin.postEvent(posted);
        assertNotNull(this.eventAdmin.awaitPostingOfEvent("topic", 1000));
        assertNull(this.eventAdmin.awaitPostingOfEvent("topic", 1));
    }

    @Test
    public void removalOfSentEventWhenSuccessfullyAwaitedMatchingOnTopic() {
        Event sent = new Event("topic", this.properties);

        this.eventAdmin.sendEvent(sent);
        assertNotNull(this.eventAdmin.awaitSendingOfEvent("topic", 1000));
        assertNull(this.eventAdmin.awaitSendingOfEvent("topic", 1));
    }

    @Test(timeout = 30000)
    public void awaitingThreadsCanBeInterruptedWhenMatchingOnTopic() throws InterruptedException {        

        final CountDownLatch latch = new CountDownLatch(1);

        Thread awaitingThread = new Thread(() -> {
            eventAdmin.awaitSendingOfEvent("topic", Long.MAX_VALUE);
            latch.countDown();
        });

        awaitingThread.start();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        ThreadInfo threadInfo;

        while ((threadInfo = threadBean.getThreadInfo(awaitingThread.getId())) == null || threadInfo.getThreadState() != State.TIMED_WAITING) {
            Thread.sleep(10);
        }

        awaitingThread.interrupt();

        latch.await();
    }
}
