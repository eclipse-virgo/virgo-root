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

package org.eclipse.virgo.nano.serviceability.dump;

/**
 * Maintains state about which {@link Throwable} was last seen for the current thread.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * State is thread local.
 * 
 */
final class FFDCExceptionState {

    private static final State STATE = new State();

    /**
     * Record that the supplied {@link Throwable} has been seen.
     * 
     * @param t the <code>Throwable</code>.
     */
    public static void record(Throwable t) {
        STATE.set(t);
    }

    /**
     * Query whether the supplied {@link Throwable} or one of its causes has been seen.
     * 
     * @param t the <code>Throwable</code>
     * @return <code>true</code> if the <code>Throwable</code> has been seen, otherwise <code>false</code>.
     */
    public static boolean seen(Throwable t) {
        boolean seen = false;
        Throwable s = t;
        do {
            if (s.equals(STATE.get())) {
                seen = true;
                break;
            }
            s = s.getCause();
        } while (s != null);
        return seen;
    }

    private static class State extends ThreadLocal<Throwable> {

        /**
         * {@inheritDoc}
         */
        @Override public String toString() {
            return String.format("FFDC Exception State [lastSeen = '" + get() + "']");
        }

    }
}
