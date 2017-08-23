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

package org.eclipse.virgo.repository.internal;

import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * <code>enum</code> of log events for Repository
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Utterly thread-safe
 * 
 */
public enum RepositoryLogEvents implements LogEvent {
    
    UNKNOWN_REPOSITORY_TYPE(1, Level.WARNING), //
    CHAIN_REFERENCES_MISSING_REPOSITORY(2, Level.WARNING), //
    DUPLICATE_REPOSITORY_IN_CHAIN(3, Level.WARNING), //
    NO_REPOSITORY_TYPE(4, Level.WARNING), //
    MISSING_SPECIFICATION(5, Level.WARNING), //
    MALFORMED_INT_PROPERTY(6, Level.WARNING), //

    REPOSITORY_TEMPORARILY_UNAVAILABLE(50, Level.WARNING), //
    REPOSITORY_AVAILABLE(51, Level.INFO), //
    REPOSITORY_INDEX_UPDATED(52, Level.INFO), //

    BRIDGE_PARSE_FAILURE(80, Level.ERROR), //
    BRIDGE_UNEXPECTED_EXCEPTION(81, Level.ERROR), //
    ARTIFACT_RECOVERED(82, Level.INFO), //

    REPOSITORY_NOT_CREATED(100, Level.WARNING), //
    REPOSITORY_NOT_AVAILABLE(101, Level.ERROR), //
    REPOSITORY_INDEX_NOT_PERSISTED(102, Level.ERROR), //
    
    ARTIFACT_NOT_PUBLISHED(200, Level.WARNING);

    private static final String SERVER_LOG_FORMAT = "%s%04d%1.1s";

    private static final String PREFIX = "RP";

    private final int code;

    private final Level level;

    private RepositoryLogEvents(int code, Level level) {
        this.code = code;
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    public String getEventCode() {
        return String.format(RepositoryLogEvents.SERVER_LOG_FORMAT, RepositoryLogEvents.PREFIX, this.code, this.level);
    }

    /**
     * {@inheritDoc}
     */
    public Level getLevel() {
        return this.level;
    }

}
