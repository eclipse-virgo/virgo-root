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

package org.eclipse.virgo.nano.core;

import javax.management.MXBean;

/**
 * Shutdown control mechanism. Calling {@link #shutdown()} will shut the kernel down gracefully whereas
 * {@link #immediateShutdown()} will kill the kernel immediately without performing any cleanup actions.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations are <strong>required</strong> to be thread-safe.
 * 
 */
@MXBean
public interface Shutdown {

    /**
     * Shuts down the Server immediately.
     */
    void immediateShutdown();

    /**
     * Shuts down the server in a graceful manner thereby minimising processing required during a subsequent restart.
     */
    void shutdown();
}
