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
package org.eclipse.virgo.nano.core;

import java.util.concurrent.TimeUnit;


/**
 * <p>
 * A <code>Signal</code> implementation that blocks until notified of completion or abortion.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
public final class BlockingAbortableSignal implements AbortableSignal {

	private volatile boolean aborted = false;
	
	private final BlockingSignal blockingSignal;
	
	public BlockingAbortableSignal() {
		this.blockingSignal = new BlockingSignal();
	}

    /** 
     * {@inheritDoc}
     */
	public void signalSuccessfulCompletion() {
		this.blockingSignal.signalSuccessfulCompletion();
	}

    /** 
     * {@inheritDoc}
     */
	public void signalFailure(final Throwable cause) {
		this.blockingSignal.signalFailure(cause);
	}

    /** 
     * {@inheritDoc}
     */
	public void signalAborted() {
		this.aborted = true;
		this.blockingSignal.signalSuccessfulCompletion();
	}
	
	public boolean isAborted(){
		return this.aborted;
	}
	
    public boolean awaitCompletion(long period, TimeUnit timeUnit) throws FailureSignalledException {
    	boolean complete = this.blockingSignal.awaitCompletion(period, timeUnit);
    	if(aborted){
    		return false;
    	}
    	return complete;
    }
	
}
