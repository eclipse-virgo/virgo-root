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

package org.eclipse.virgo.kernel.services.concurrent.management;

import org.eclipse.virgo.kernel.services.concurrent.ExecutorServiceStatistics;

/**
 * A utility to encapsulate the management exporting of a {@link ExecutorServiceStatistics} object
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations should be threadsafe
 * 
 */
public interface ExecutorServiceExporter {

    void export(ExecutorServiceStatistics executorService);
}
