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
 * {@link ModuleContextEvent} is the root of a hierarchy of events related to {@link ModuleContext ModuleContexts}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is thread safe.
 *
 */
public abstract class ModuleContextEvent {
    
    private final ModuleContext moduleContext;
    
    private final Bundle bundle;

    protected ModuleContextEvent(ModuleContext moduleContext, Bundle bundle) {
        this.moduleContext = moduleContext;
        this.bundle = bundle;
    }

    
    /**
     * Get the {@link ModuleContext} associated with this event.
     * 
     * @return the <code>ModuleContext</code> associated with this event 
     */
    public ModuleContext getModuleContext() {
        return moduleContext;
    }

    
    /**
     * Get the {@link Bundle} associated with this event.
     * 
     * @return the <code>Bundle</code> associated with this event 
     */
    public Bundle getBundle() {
        return bundle;
    }

}
