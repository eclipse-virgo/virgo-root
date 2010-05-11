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

package org.eclipse.virgo.kernel.osgi.framework.support;

import org.eclipse.virgo.kernel.osgi.framework.OsgiConfiguration;

/**
 * Abstract implementation of {@link OsgiConfiguration}.
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Thread-safe.
 * 
 */
public final class ImmutableOsgiConfiguration implements OsgiConfiguration {

    private final boolean consoleEnabled;

    private final int consolePort;

    /**
     * Creates a new <code>OsgiConfiguration</code> with the repository search paths.
     * @param consoleEnabled whether console is enabled
     * @param consolePort port number for console
     */
    public ImmutableOsgiConfiguration(boolean consoleEnabled, int consolePort) {
        this.consoleEnabled = consoleEnabled;
        this.consolePort = consolePort;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConsoleEnabled() {
        return this.consoleEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public int getConsolePort() {
        return this.consolePort;
    }
}
