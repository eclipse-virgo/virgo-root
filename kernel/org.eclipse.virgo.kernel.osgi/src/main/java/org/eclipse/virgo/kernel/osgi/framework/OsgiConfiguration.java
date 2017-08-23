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

package org.eclipse.virgo.kernel.osgi.framework;

/**
 * Configuration data for an {@link OsgiFramework}.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementations <strong>must</strong> be threadsafe.
 * 
 */
public interface OsgiConfiguration {

    /**
     * Whether or not the console is enabled
     * 
     * @return <code>true</code> if the console is enabled, otherwise <code>false</code>.
     */
    boolean isConsoleEnabled();

    /**
     * Returns the port upon which the console should listen
     * 
     * @return the console's port
     */
    int getConsolePort();
}
