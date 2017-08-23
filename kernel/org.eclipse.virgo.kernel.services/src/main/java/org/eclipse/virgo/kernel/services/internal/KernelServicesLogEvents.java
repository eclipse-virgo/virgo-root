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

package org.eclipse.virgo.kernel.services.internal;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * Defines all the {@link LogEvent LogEvents} for the kernel services bundle.
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementation is immutable.
 * 
 */
public enum KernelServicesLogEvents implements LogEvent {

    KERNEL_REPOSITORY_CHAIN_EMPTY(1, Level.INFO), //
    
    KERNEL_REPOSITORY_CHAIN_ENTRY_NOT_VALID(2, Level.ERROR);

    private static final String PREFIX = "KS";

    private final LogEventDelegate delegate;

    private KernelServicesLogEvents(int code, Level level) {
        this.delegate = new LogEventDelegate(PREFIX, code, level);
    }

    /**
     * {@inheritDoc}
     */
    public String getEventCode() {
        return this.delegate.getEventCode();
    }

    /**
     * {@inheritDoc}
     */
    public Level getLevel() {
        return this.delegate.getLevel();
    }

}
