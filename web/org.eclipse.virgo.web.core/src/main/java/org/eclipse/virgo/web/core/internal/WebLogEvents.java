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

package org.eclipse.virgo.web.core.internal;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * Custom {@link LogEvent} enums for the web subsystem.
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Thread-safe.
 * 
 */
public enum WebLogEvents implements LogEvent {

    STARTING_WEB_BUNDLE(0, Level.INFO), //
    STARTED_WEB_BUNDLE(1, Level.INFO), //
    STOPPING_WEB_BUNDLE(2, Level.INFO), //
    STOPPED_WEB_BUNDLE(3, Level.INFO), //
    WEB_BUNDLE_FAILED_CONTEXT_PATH_USED(4, Level.ERROR), //
    WEB_BUNDLE_FAILED(5, Level.ERROR), //
    DEFAULTING_WAB_HEADERS(6, Level.WARNING);

    private static final String PREFIX = "WE";

    private final LogEventDelegate delegate;

    private WebLogEvents(int code, Level level) {
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
