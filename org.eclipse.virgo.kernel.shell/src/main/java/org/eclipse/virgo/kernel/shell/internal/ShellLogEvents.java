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

package org.eclipse.virgo.kernel.shell.internal;

import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * <p>
 * {@link LogEvent} for the OSGi provisioning bundle.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public enum ShellLogEvents implements LogEvent {
    
    SERVER_CONSOLE_PORT(1, Level.INFO),
    SERVER_SHELL_PORT_IN_USE(2, Level.ERROR),
    SERVER_SHELL_UNSUPPORTED(3, Level.WARNING);

    private static final String PREFIX = "SH";

    private final int code;

    private final Level level;

    private ShellLogEvents(int code, Level level) {            
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
