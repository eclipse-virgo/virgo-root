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

package org.eclipse.virgo.kernel.shell.internal;


/**
 * <p>
 * Provides a mechanism for a shell to inform interested parties when it is going to exit.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * ExitCallback implementations should be threadsafe
 *
 */
interface ExitCallback {

    /**
     * To be called by the shell when it is about to exit.
     * There is no need to call this when the shell is being stopped.
     */
    void onExit();
    
}
