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
import java.lang.management.ThreadMXBean;

/**
 * An implementation that exports the Thread bean
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe
 *
 */
public class ThreadingSystemManagementExporter extends AbstractSystemManagementExporter<ThreadMXBean> {

	private static final String THREADING = "Threading";

	@Override
    ThreadMXBean getBean() {
	    return ManagementFactory.getThreadMXBean();
    }

	@Override
    String getName() {
	    return THREADING;
    }

}
