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
package org.eclipse.virgo.kernel.install.artifact.internal;

import org.eclipse.virgo.nano.core.AbortableSignal;

class StubAbortableSignal implements AbortableSignal {

    private volatile boolean complete = false;
    
    private volatile boolean aborted = false;
    
    private volatile Throwable cause = null;

    /**
     * {@inheritDoc}
     */
    public void signalSuccessfulCompletion() {
        this.complete = true;
    }

    /**
     * {@inheritDoc}
     */
    public void signalFailure(Throwable t) {
        this.complete = true;
        this.cause = t;
    }

    /**
     * {@inheritDoc}
     */
	public void signalAborted() {
        this.complete = true;
        this.aborted = true;
	}
    
    public boolean isComplete() {
        return this.complete;
    }
    
    public boolean isAborted() {
        return this.aborted;
    }
    
    public Throwable getCause() {
        return this.cause;
    }
    
}