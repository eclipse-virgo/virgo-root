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
import java.lang.management.RuntimeMXBean;

/**
 * An implementation that exports the Runtime bean
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe.
 *
 */
public class RuntimeSystemManagementExporter extends AbstractSystemManagementExporter<RuntimeMXBean> {

	private static final String RUNTIME = "Runtime";
	
	@Override
    RuntimeMXBean getBean() {
	    return ManagementFactory.getRuntimeMXBean();
    }

	@Override
    String getName() {
	    return RUNTIME;
    }

}
