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

package org.eclipse.virgo.kernel.osgi.framework;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * {@link LogEvent} for the OSGi provisioning bundle.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public enum OsgiFrameworkLogEvents implements LogEvent {

    REGION_IMPORTS_PARSE_FAILED(1, Level.ERROR), //
    REGION_IMPORT_NO_MATCH(2, Level.WARNING), //
    // 3 has been moved to the user region bundle and is unused here.
    // 4 has been moved to the user region bundle and is unused here. 
    USER_REGION_CONFIGURATION_UNAVAILABLE(10, Level.ERROR),
    OSGI_CONSOLE_PORT(100, Level.INFO), //
    OSGI_CONSOLE_PORT_IN_USE(101, Level.ERROR);

    private static final String PREFIX = "OF";

    private final LogEventDelegate delegate;

    private OsgiFrameworkLogEvents(int code, Level level) {
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
