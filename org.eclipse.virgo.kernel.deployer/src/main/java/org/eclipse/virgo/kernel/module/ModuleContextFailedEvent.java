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

package org.eclipse.virgo.kernel.module;

import org.osgi.framework.Bundle;


/**
 * {@link ModuleContextFailedEvent} is used to notify failure to construct a {@link ModuleContext}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is thread safe.
 *
 */
public final class ModuleContextFailedEvent extends ModuleContextEvent {

    private final Throwable failureCause;

    public ModuleContextFailedEvent(ModuleContext moduleContext, Bundle bundle, Throwable failureCause) {
        super(moduleContext, bundle);
        this.failureCause = failureCause;
    }
    
    /**
     * Get the cause of the failure associated with this event.
     * 
     * @return the {@link Throwable} cause
     */
    public Throwable getFailureCause() {
        return failureCause;
    }
    
}
