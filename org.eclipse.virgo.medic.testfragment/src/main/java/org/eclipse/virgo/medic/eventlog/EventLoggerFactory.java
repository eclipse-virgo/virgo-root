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

import org.osgi.framework.Bundle;

/**
 * An <code>EventLoggerFactory</code> is used to create an <code>EventLogger</code> instance that will search a specific
 * bundle, in addition to the EventLogger implementation's bundle, for {@link ResourceBundle ResourceBundles}.
 * <p />
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread-safe.
 */
public interface EventLoggerFactory {

    /**
     * Creates a new <code>EventLogger</code> that will search the supplied bundle, in addition to the
     * <code>EventLogger</code> implementation's bundle, for {@link ResourceBundle ResourceBundles}.
     * 
     * @param bundle The <code>Bundle</code> to search for <code>ResourceBundle</code>s.
     * 
     * @return The <code>EventLogger</code>
     */
    public EventLogger createEventLogger(Bundle bundle);
}
