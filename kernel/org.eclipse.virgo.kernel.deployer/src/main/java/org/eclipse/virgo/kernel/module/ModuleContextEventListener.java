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

/**
 * {@link ModuleContextEventListener} is used to listen for events relating to {@link ModuleContext ModuleContexts}. <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface ModuleContextEventListener {
    
    /**
     * Notify this listener of the given {@link ModuleContextEvent}.
     * 
     * @param moduleContextEvent the {@link ModuleContextEventListener}
     */
    void onEvent(ModuleContextEvent moduleContextEvent);

}
