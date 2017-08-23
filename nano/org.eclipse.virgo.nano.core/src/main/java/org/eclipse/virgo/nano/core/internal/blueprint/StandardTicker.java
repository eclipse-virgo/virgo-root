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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;




/**
 * {@link StandardTicker} provides a heart-beat for tracking unanticipated delays. The heart-beat is configurable using a
 * policy such as {@link ExponentialHeartBeatPolicy} which lengthens the heart-beat interval exponentially over time
 * until it reaches a fixed upper bound. On each heart-beat until the ticker is cancelled, an action is called.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 * @param <V> the result type of the action taken on each heart-beat
 */
public final class StandardTicker<V> implements Ticker, Callable<V> {

    private static final long OVERDUE = -1;

    private final long creationTimeMillis;

    private long lastTickMillis;

    private final HeartBeatPolicy heartBeatPolicy;

    private long heartBeatIntervalMillis;
    
    private boolean tickedAtLeastOnce = false;
    
    private boolean cancelled = false;
    
    private ScheduledFuture<V> scheduledFuture;

    private final Callable<V> action;

    private final ScheduledExecutorService scheduledExecutorService;

    private Object monitor = new Object();

    /**
     * Create a {@link Ticker} with the given heart-beat policy, action to be called on each tick, and executor service
     * for scheduling ticks and set it ticking.
     * 
     * @param <V> the action's result type
     * @param heartBeatPolicy a policy which determines the possibly variable intervals between heartbeats
     * @param action the thread safe action to be called on each tick
     * @param scheduledExecutorService the executor service for scheduling ticks
     * @return the constructed and ticking ticker
     */
    public static <V> Ticker createTicker(HeartBeatPolicy heartBeatPolicy, Callable<V> action, ScheduledExecutorService scheduledExecutorService) {
        StandardTicker<V> ticker = new StandardTicker<V>(heartBeatPolicy, action, scheduledExecutorService);
        ticker.start();
        return ticker;
    }

    /**
     * Create a {@link Ticker} with an exponential heart beat policy and a given action to be called on each tick and an
     * executor service for scheduling ticks and set it ticking. The exponential heart beat policy has the given initial
     * interval which then increases by the given percentage on each tick until the given maximum interval is reached.
     * 
     * @param <V> the action's result type
     * @param initialHeartbeatIntervalMillis the initial interval
     * @param heartBeatIncreasePercentage the percentage increase on each tick
     * @param maxHeartBeatIntervalMillis the maximum interval
     * @param action the thread safe action to be called on each tick
     * @param scheduledExecutorService the executor service for scheduling ticks
     * @return the constructed and ticking ticker
     */
    public static <V> Ticker createExponentialTicker(long initialHeartbeatIntervalMillis, long heartBeatIncreasePercentage,
        long maxHeartBeatIntervalMillis, Callable<V> action, ScheduledExecutorService scheduledExecutorService) {
        return createTicker(new ExponentialHeartBeatPolicy(initialHeartbeatIntervalMillis, heartBeatIncreasePercentage, maxHeartBeatIntervalMillis),
            action, scheduledExecutorService);
    }

    /**
     * Construct a Ticker with the given heart beat policy, action to be called on each tick, and executor service for
     * scheduling ticks.
     * 
     * @param heartBeatPolicy a policy which determines the possibly variable intervals between heartbeats
     * @param action the thread safe action to be called on each tick
     * @param scheduledExecutorService the executor service for scheduling ticks
     */
    private StandardTicker(HeartBeatPolicy heartBeatPolicy, Callable<V> action, ScheduledExecutorService scheduledExecutorService) {
        this.heartBeatPolicy = heartBeatPolicy;
        this.heartBeatIntervalMillis = this.heartBeatPolicy.getNextHeartBeatIntervalMillis();
        this.creationTimeMillis = System.currentTimeMillis();
        this.lastTickMillis = this.creationTimeMillis;
        this.action = action;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * Start this ticker ticking.
     */
    private void start() {
        if (getIntervalToNextTickMillis() == OVERDUE) {
            try {
                this.call();
            } catch (Exception e) {
            }
        } else {
            scheduleNextTick();
        }
    }

    /**
     * Schedule the next tick of this ticker.
     */
    private void scheduleNextTick() {
        synchronized (this.monitor) {
            if (!this.cancelled) {
                this.scheduledFuture = this.scheduledExecutorService.schedule(this, getIntervalToNextTickMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public V call() throws Exception {
        boolean ticked = tick();
        scheduleNextTick();
        return ticked ? this.action.call() : null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean cancel() {
        synchronized (this.monitor) {
            this.cancelled = true;
            this.scheduledFuture.cancel(true);
            return this.tickedAtLeastOnce;
        }
    }

    /**
     * Determine whether a tick is due and, if so, update the ticker state to count down to the next tick and return
     * <code>true</code>. If no tick is due, do not update the ticker state and return <code>false</code>.
     * 
     * @return <code>true</code> if and only if the ticker tickedAtLeastOnce
     */
    private boolean tick() {
        synchronized (this.monitor) {
            boolean ticked = false;
            if (!cancelled && getIntervalToNextTickMillis() == OVERDUE) {
                ticked = true;
                this.lastTickMillis = getCurrentTimeMillis();
                this.heartBeatIntervalMillis = this.heartBeatPolicy.getNextHeartBeatIntervalMillis();
            }
            this.tickedAtLeastOnce = this.tickedAtLeastOnce || ticked;
            return ticked;
        }
    }

    /**
     * Get the current time.
     * 
     * Pre-condition: the monitor must be held.
     * 
     * Post-condition: result >= this.lastTickMillis.
     * 
     * @return the current time in milliseconds
     */
    private long getCurrentTimeMillis() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < this.lastTickMillis) {
        	throw new IllegalArgumentException("Time must not go backwards");
        }
        return currentTimeMillis;
    }

    /**
     * Get the time interval until the next tick is due, or OVERDUE if the next tick is overdue.
     * 
     * @return the time interval in milliseconds, or OVERDUE if the next tick is overdue
     */
    private long getIntervalToNextTickMillis() {
        synchronized (this.monitor) {
            long intervalSinceLastTickMillis = getCurrentTimeMillis() - this.lastTickMillis;
            return intervalSinceLastTickMillis < this.heartBeatIntervalMillis ? this.heartBeatIntervalMillis - intervalSinceLastTickMillis : OVERDUE;
        }
    }

    /**
     * {@link ExponentialHeartBeatPolicy} is a {@link HeartBeatPolicy} which returns intervals starting with a given
     * initial interval and increasing by a given percentage up to a given maximum interval.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * This class is thread safe.
     */
    private final static class ExponentialHeartBeatPolicy implements HeartBeatPolicy {

        private final long maxHeartBeatIntervalMillis;

        private final long heartBeatIncreasePercentage;

        private AtomicLong heartBeatIntervalMillis;

        /**
         * Construct a {@link Ticker.HeartBeatPolicy HeartBeatPolicy} which the given initial interval which then increases by the given
         * percentage on each tick until the given maximum interval is reached.
         * 
         * @param initialHeartbeatIntervalMillis the initial interval
         * @param heartBeatIncreasePercentage the percentage increase on each tick
         * @param maxHeartBeatIntervalMillis the maximum interval
         */
        public ExponentialHeartBeatPolicy(long initialHeartbeatIntervalMillis, long heartBeatIncreasePercentage, long maxHeartBeatIntervalMillis) {
            this.heartBeatIntervalMillis = new AtomicLong(initialHeartbeatIntervalMillis);
            this.heartBeatIncreasePercentage = heartBeatIncreasePercentage;
            this.maxHeartBeatIntervalMillis = maxHeartBeatIntervalMillis;
        }

        /**
         * {@inheritDoc}
         */
        public long getNextHeartBeatIntervalMillis() {
            boolean success = false;
            long nextHeartBeatIntervalMillis = 0;
            while (!success) {
                nextHeartBeatIntervalMillis = this.heartBeatIntervalMillis.get();
                if (nextHeartBeatIntervalMillis < maxHeartBeatIntervalMillis) {
                    long newHeartBeatIntervalMillis = Math.min((nextHeartBeatIntervalMillis * (100 + heartBeatIncreasePercentage)) / 100,
                        maxHeartBeatIntervalMillis);
                    success = this.heartBeatIntervalMillis.compareAndSet(nextHeartBeatIntervalMillis, newHeartBeatIntervalMillis);
                } else {
                    success = true;
                }
            }
            return nextHeartBeatIntervalMillis;
        }

    }

}
