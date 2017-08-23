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

package org.eclipse.virgo.nano.diagnostics;

import org.eclipse.virgo.nano.serviceability.LogEventDelegate;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * Defines all the {@link LogEvent LogEvents} for the kernel subsystem.
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementation is immutable.
 * 
 */
public enum KernelLogEvents implements LogEvent {
    KERNEL_STARTING(1, Level.INFO), //  
    KERNEL_STARTED(2, Level.INFO), //
    KERNEL_START_FAILED(3, Level.ERROR), //
    KERNEL_START_TIMED_OUT(4, Level.ERROR), //
    KERNEL_PLAN_ARGUMENTS_INCORRECT(5, Level.WARNING), //
    KERNEL_EVENT_START_ABORTED(6, Level.ERROR), //
    VIRGO_STARTED(7, Level.INFO), //
    VIRGO_STARTED_NOTIME(8, Level.INFO), //
    
    SHUTDOWN_INITIATED(10, Level.INFO), //
    IMMEDIATE_SHUTDOWN_INITIATED(11, Level.INFO), //
    SHUTDOWN_HALTED(12, Level.ERROR), //
    
    APPLICATION_CONTEXT_DEPENDENCY_DELAYED(100, Level.WARNING), //
    APPLICATION_CONTEXT_DEPENDENCY_SATISFIED(101, Level.INFO), //
    APPLICATION_CONTEXT_DEPENDENCY_TIMED_OUT(102, Level.ERROR), //
    
    OVF_CONFIGURATION_FILE_DOES_NOT_EXIST(200, Level.WARNING), //
    OVF_READ_ERROR(201, Level.ERROR), //
    
    OLD_SCOPING_PROPERTY_USED(300, Level.WARNING);

    private static final String PREFIX = "KE";

    private final LogEventDelegate delegate;

    private KernelLogEvents(int code, Level level) {
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
