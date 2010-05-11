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
 * Interface for local shell implementations.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Implementations should be thread safe
 *
 */
interface LocalShell extends Runnable{
    
    /**
     * When the shell closes down all registered callbacks will be synchronously informed
     * 
     * @param exitCallback
     */
    void addExitCallback(ExitCallback exitCallback);

}
