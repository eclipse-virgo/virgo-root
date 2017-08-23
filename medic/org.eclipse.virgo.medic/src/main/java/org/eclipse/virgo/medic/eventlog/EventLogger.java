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

package org.eclipse.virgo.medic.eventlog;

import java.util.ResourceBundle;

/**
 * An <code>EventLogger</code> provides support for logging events as human-readable, potentially internationalized,
 * messages identified by a unique code.
 * <p />
 * An <code>EventLogger</code> instance can be obtained directly from the service registry. Such instances will search
 * the {@link org.osgi.framework.Bundle} that obtained the instance, and the <code>EventLogger</code> implementations <code>Bundle</code>
 * for a properties file that contains entry for the an event's code. The implementation bundle is searched to support
 * providing properties files in fragment bundles which are attached to the implementation.
 * <p />
 * Alternatively, an <code>EventLogger</code> can be obtained from an {@link EventLoggerFactory} allowing a specific
 * <code>Bundle</code> to be supplied that will be used to search for properties files.
 * <p />
 * The algorithm used to find properties files matches that described in
 * {@link ResourceBundle#getBundle(String, java.util.Locale, ClassLoader) ResourceBundle}, with the exception that no
 * searching for class files is performed.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread-safe.
 */
public interface EventLogger {

    /**
     * Logs an event in the form of a message identified by the supplied code. The message is resolved by searching the
     * available {@link ResourceBundle ResourceBundles} for an entry with the key that matches the code.
     * 
     * The event is logged at the supplied level, with the supplied inserts being applied to the message.
     * 
     * @param code The code of the message to be logged
     * @param level The level at which the event should be logged
     * @param inserts The inserts for the message
     */
    void log(String code, Level level, Object... inserts);

    /**
     * Logs an event in the form of a message identified by the supplied code. The message is resolved by searching the
     * available {@link ResourceBundle ResourceBundles} for an entry with the key that matches the code.
     * 
     * The event is logged at the supplied level, with the supplied inserts being applied to the message.
     * 
     * @param logEvent The log event to be logged
     * @param inserts The inserts for the message
     */
    void log(LogEvent logEvent, Object... inserts);

    /**
     * Logs an event in the form of a message identified by the supplied code. The message is resolved by searching the
     * available {@link ResourceBundle ResourceBundles} for an entry with the key that matches the code.
     * 
     * The event is logged at the supplied level, with the supplied inserts being applied to the message. The supplied
     * <code>Throwable</code> is logged with the message.
     * 
     * @param code The code of the message to be logged
     * @param level The level at which the event should be logged
     * @param throwable The <code>Throwable</code> to be logged
     * @param inserts The inserts for the message
     */
    void log(String code, Level level, Throwable throwable, Object... inserts);

    /**
     * Logs an event in the form of a message identified by the supplied code. The message is resolved by searching the
     * available {@link ResourceBundle ResourceBundles} for an entry with the key that matches the code.
     * 
     * The event is logged at the supplied level, with the supplied inserts being applied to the message. The supplied
     * <code>Throwable</code> is logged with the message.
     * 
     * @param logEvent The log event to be logged
     * @param throwable The <code>Throwable</code> to be logged
     * @param inserts The inserts for the message
     */
    void log(LogEvent logEvent, Throwable throwable, Object... inserts);
}
