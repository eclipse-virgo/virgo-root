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

package org.eclipse.virgo.kernel.services.concurrent;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

/**
 */
public final class ThreadPoolUtils {

    private static final RejectedExecutionHandler DEFAULT_HANDLER = new AbortPolicy();

    public static ThreadFactory createThreadFactory(String poolName) {
        return new NamedThreadFactory(poolName);
    }

    static RejectedExecutionHandler determineHandler(RejectedExecutionHandler handler) {
        return (handler == null ? DEFAULT_HANDLER : handler);
    }
}
