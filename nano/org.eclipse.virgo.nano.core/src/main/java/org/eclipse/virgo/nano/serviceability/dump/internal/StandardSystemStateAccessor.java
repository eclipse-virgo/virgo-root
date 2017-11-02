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

import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;

/**
 * Standard implementation of {@link SystemStateAccessor}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe.
 *
 */
final class StandardSystemStateAccessor implements SystemStateAccessor {
    
    private final PlatformAdmin platformAdmin;
            
    StandardSystemStateAccessor(PlatformAdmin platformAdmin) {
        this.platformAdmin = platformAdmin;
    }

    /** 
     * {@inheritDoc}
     */
    public State getSystemState() {
        return this.platformAdmin.getState();
    }
}
