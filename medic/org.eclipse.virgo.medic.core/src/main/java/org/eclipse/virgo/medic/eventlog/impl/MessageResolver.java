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

package org.eclipse.virgo.medic.eventlog.impl;

import java.util.Locale;

/**
 * A LogEventMessageResolver is used to resolve the message of a logged event.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface MessageResolver {

    /**
     * Resolves the message identified by the supplied event code.
     * @param eventCode The code identifying the message to be resolved
     * @return The resolved message, or <code>null</code> if no message could be found
     */
    String resolveLogEventMessage(String eventCode);

    /**
     * Resolves the message identified by the supplied event code in the supplied {@link Locale}.
     * 
     * @param eventCode The code identifying the message is to be resolved
     * @param locale the {@link Locale} in which the message is to be resolved
     * @return The resolved message, or <code>null</code> if no message could be found
     */
    String resolveLogEventMessage(String eventCode, Locale locale);
}
