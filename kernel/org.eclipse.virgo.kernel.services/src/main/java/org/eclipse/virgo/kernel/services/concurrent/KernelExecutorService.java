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

import java.util.concurrent.ExecutorService;

/**
 * Extension to {@link ExecutorService} that provides access to metrics about the <code>Executor</code>.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Semantics defined in {@link ExecutorService} and {@link ExecutorServiceStatistics}.
 * 
 */
public interface KernelExecutorService extends ExecutorService, ExecutorServiceStatistics {

   
}
