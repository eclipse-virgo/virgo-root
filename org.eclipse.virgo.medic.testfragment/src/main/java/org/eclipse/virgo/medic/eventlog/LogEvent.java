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

/**
 * A <code>LogEvent</code> encapsulates the details of an event that is to be logged.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface LogEvent {

    /**
     * Returns the code that uniquely identifies this <code>LogEvent</code>. Typically, this code is used to locate the
     * message for the event in a resource bundle.
     * 
     * @return The event's code
     */
    public String getEventCode();

    /**
     * Returns the {@link Level} at which this <code>LogEvent</code> should be published.
     * 
     * @return The level of the event.
     */
    public Level getLevel();
}
