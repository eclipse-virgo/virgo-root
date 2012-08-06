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


/**
 * {@link Ticker} provides a heartbeat interface for tracking unanticipated delays. The heartbeat is configurable using
 * a {@link HeartBeatPolicy}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface Ticker {

    /**
     * Cancel this Ticker.
     *
     * @return <code>true</code> if and only this Ticker has ticked at least once
     */
    boolean cancel();

    /**
     * {@link HeartBeatPolicy} is an interface for configuring heartbeat intervals.
     * <p />
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * Implementations of this interface should be thread safe to avoid potential abuse.
     */
    public static interface HeartBeatPolicy {

        /**
         * Return the heartbeat interval and update the next interval according to the policy.
         * 
         * @return the heartbeat interval in milliseconds
         */
        long getNextHeartBeatIntervalMillis();
    }

}
