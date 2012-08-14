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

package org.eclipse.virgo.kernel.userregion.internal;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * User region log events.
 * <p />
 * 
 */
public enum UserRegionLogEvents implements LogEvent {

    SYSTEM_ARTIFACTS_DEPLOYED(1, Level.INFO), //
    INITIAL_ARTIFACT_DEPLOYMENT_FAILED(2, Level.ERROR), //
    SYSTEM_BUNDLE_OVERLAP(3, Level.WARNING), //
    ALTERNATE_INSTRUMENTED_LIBRARY_FOUND(4, Level.WARNING),
    KERNEL_SERVICE_NOT_AVAILABLE(5, Level.ERROR),
    USERREGION_START_INTERRUPTED(6, Level.ERROR);
    
    private static final String PREFIX = "UR";

    private final LogEventDelegate delegate;

    private UserRegionLogEvents(int code, Level level) {
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
