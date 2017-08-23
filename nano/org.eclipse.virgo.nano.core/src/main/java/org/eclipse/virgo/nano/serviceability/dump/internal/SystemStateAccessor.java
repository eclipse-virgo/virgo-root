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

package org.eclipse.virgo.nano.serviceability.dump.internal;

import org.eclipse.osgi.service.resolver.State;


/**
 * A <code>SystemStateAccessor</code> provides access to the live system {@link State}.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
interface SystemStateAccessor {
    
    /**
     * Returns the {@link State} for the system
     * @return the system <code>State</code>.
     */
    State getSystemState();
}
