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

package org.eclipse.virgo.kernel.deployer.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.nano.core.AbortableSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link AbortableSignalJunction} provides a collection of signals of a given size that join to drive a given signal. The given
 * signal is driven for completion when any of the signals in the collection is driven for failure or all the signals in
 * the collection are driven for successful completion. The given signal is driven at most once.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class AbortableSignalJunction {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbortableSignalJunction.class);

    private final AbortableSignal signal;

    private final List<AbortableSignal> subSignals;

    private final AtomicInteger incompleteCount;

    private final AtomicBoolean failureSignalled = new AtomicBoolean(false);
    
    private final AtomicBoolean abortionSignalled = new AtomicBoolean(false);

    /**
     * Constructs a {@link AbortableSignalJunction} of the given size for the given signal.
     * 
     * @param signal the signal to be controlled
     * @param size the number of signals in the collection
     */
    public AbortableSignalJunction(AbortableSignal signal, int size) {
        this.signal = signal;
        List<AbortableSignal> s = new ArrayList<AbortableSignal>();
        for (int i = 0; i < size; i++) {
            s.add(new SubSignal());
        }
        this.subSignals = Collections.unmodifiableList(s);
        this.incompleteCount = new AtomicInteger(size);
        if (size <= 0) {
            if (this.signal != null) {
                this.signal.signalSuccessfulCompletion();
            }
        }
    }

    /**
     * Gets the signals in the collection.
     * 
     * @return an unmodifiable {@link Set} of signals in the collection
     */
    public List<AbortableSignal> getSignals() {
        return this.subSignals;
    }
    
    public boolean failed() {
        return this.failureSignalled.get();
    }
    
    public boolean aborted() {
        return this.abortionSignalled.get();
    }
    
    private void subSignalFailed(Throwable cause) {
        // Ensure the incomplete count is zero.
        int i = AbortableSignalJunction.this.incompleteCount.get();
        
        LOGGER.debug("SubSignal failed. {} has {} incomplete signals", this, i);
        
        while (i > 0 && !AbortableSignalJunction.this.incompleteCount.compareAndSet(i, 0)) {
            i = AbortableSignalJunction.this.incompleteCount.get();
        }

        // If this invocation took the count to zero, drive failure.
        if (i > 0) {
            if (AbortableSignalJunction.this.signal != null) {
            	LOGGER.debug("{} signalling failure", this);
                AbortableSignalJunction.this.signal.signalFailure(cause);
                this.failureSignalled.set(true);
            }
        }
    }

    private void subSignalAborted() {
        // Ensure the incomplete count is zero.
        int i = AbortableSignalJunction.this.incompleteCount.get();
        
        LOGGER.debug("SubSignal aborted. {} has {} incomplete signals", this, i);
        
        while (i > 0 && !AbortableSignalJunction.this.incompleteCount.compareAndSet(i, 0)) {
            i = AbortableSignalJunction.this.incompleteCount.get();
        }

        // If this invocation took the count to zero, drive failure.
        if (i > 0) {
            if (AbortableSignalJunction.this.signal != null) {
            	LOGGER.debug("{} signalling aborted", this);
                AbortableSignalJunction.this.signal.signalAborted();
                this.abortionSignalled.set(true);
            }
        }
    }

    private void subSignalSucceeded() {
        // Decrement the incomplete count.
        int incomplete = AbortableSignalJunction.this.incompleteCount.decrementAndGet();
        
        LOGGER.debug("SubSignal succeeded. {} now has {} incomplete signals", this, incomplete);
        
		if (incomplete == 0) {
            // If this invocation took the count to zero, drive successful completion.
            if (AbortableSignalJunction.this.signal != null) {
            	LOGGER.debug("{} has no incomplete signals. Signalling success", this);
                AbortableSignalJunction.this.signal.signalSuccessfulCompletion();
            }
        }
    }

    /**
     * {@link SubSignal} is a signal that reports completion to its {@link AbortableSignalJunction} once and only once.
     * 
     */
    private class SubSignal implements AbortableSignal {

        private final AtomicBoolean complete = new AtomicBoolean(false);

        public void signalFailure(Throwable cause) {
            if (this.complete.compareAndSet(false, true)) {
            	LOGGER.debug("SubSignal {} signalling failure", this);
                subSignalFailed(cause);
            }
        }

        public void signalSuccessfulCompletion() {
            if (this.complete.compareAndSet(false, true)) {
            	LOGGER.debug("SubSignal {} signalling success", this);
                subSignalSucceeded();
            }
        }

		public void signalAborted() {
            if (this.complete.compareAndSet(false, true)) {
            	LOGGER.debug("SubSignal {} signalling abortion", this);
                subSignalAborted();
            }
		}
    }
}
