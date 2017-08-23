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

package org.eclipse.virgo.apps.repository.core.internal;

import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * <code>enum</code> of {@link LogEvent}s for the Hosted Repository code.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * utterly thread-safe
 * 
 */
public enum HostedRepositoryLogEvents implements LogEvent {
    /** 
     * The Hosted Repository configuration is incorrect. */
    CONFIGURATION_EXCEPTION(1, Level.WARNING),
    /** 
     * The repository '{}' in a hosted repository configuration was not created. */
    REPOSITORY_EXCEPTION(2, Level.WARNING),
    /** 
     * The host address for the repository '{}' cannot be determined. */
    HOST_ADDRESS_EXCEPTION(3, Level.WARNING),
    /** 
     * The chain definition '{}' is ignored in a hosted repository configuration. */
    CHAIN_NON_EMPTY(4, Level.WARNING),
    /**
     * The repository '{}' in a hosted repository configuration is not supported and is ignored. */
    NON_SUPPORTED_REPOSITORY(5, Level.WARNING);

    private static final String SERVER_LOG_FORMAT = "%s%04d%1.1s";

    private static final String PREFIX = "HR";

    private final int code;

    private final Level level;

    private HostedRepositoryLogEvents(int code, Level level) {
        this.code = code;
        this.level = level;
    }

    public String getEventCode() {
        return String.format(HostedRepositoryLogEvents.SERVER_LOG_FORMAT, HostedRepositoryLogEvents.PREFIX, this.code, this.level);
    }

    public Level getLevel() {
        return this.level;
    }

}
