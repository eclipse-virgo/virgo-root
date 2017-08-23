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

package org.eclipse.virgo.kernel.install.environment;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * An <code>InstallLog</code> is associated with an {@link InstallEnvironment} and can be used to log work that is
 * performed within that environment for diagnostic purposes.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface InstallLog extends EventLogger {

    /**
     * Logs an entry in the log from the supplied source. The message may be in String format form with the supplied
     * arguments being applied to the message
     * 
     * @param source The entry's source
     * @param message The entry's message
     * @param arguments The message's arguments
     */
    void log(Object source, String message, String... arguments);

    /**
     * Logs the given {@link Throwable}. The implementation logs with three implicit inserts consisting of the type,
     * name, and version of the root install artifact being processed plus the given additional inserts.
     * 
     * @param logEvent the log event message to be issued
     * @param cause the cause of the failure
     * @param insert additional inserts for the log event message
     */
    void logFailure(LogEvent logEvent, Throwable cause, Object... insert);
}
