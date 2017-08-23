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

package org.eclipse.virgo.kernel.management.internal.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * An implementation that exports the Memory bean
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe.
 *
 */
public class MemorySystemManagementExporter extends AbstractSystemManagementExporter<MemoryMXBean> {

	private static final String MEMORY = "Memory";

	@Override
    MemoryMXBean getBean() {
	    return ManagementFactory.getMemoryMXBean();
    }

	@Override
    String getName() {
	    return MEMORY;
    }

}
