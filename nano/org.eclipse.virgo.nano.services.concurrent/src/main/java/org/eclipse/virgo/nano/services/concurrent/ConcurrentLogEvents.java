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

package org.eclipse.virgo.nano.services.concurrent;

import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * {@link LogEvent} for the concurrent subsystem.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public enum ConcurrentLogEvents implements LogEvent {

    DEADLOCK_DETECTED(0, Level.ERROR);

    private static final String PREFIX = "CC";
    
    private final int code;
    
    private final Level level;

    private ConcurrentLogEvents(int code, Level level) {
        this.code = code;
        this.level = level;        
    }

    /**
     * {@inheritDoc}
     */
    public String getEventCode() {
        return String.format("%s%04d%1.1s", PREFIX, this.code, this.level);
    }

    /**
     * {@inheritDoc}
     */
    public Level getLevel() {
        return this.level;
    }

}
