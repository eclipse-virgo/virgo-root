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
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * An implementation that exports the Memory Pool beans
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe.
 *
 */
public class MemoryPoolSystemManagementExporter extends AbstractMultiBeanSystemManagementExporter<MemoryPoolMXBean> {

	private static final String OBJECT_NAME_PATTERN = "%s:category=System Information,type=Memory Pool,name=%s";

	@Override
	List<MemoryPoolMXBean> getBeans() {
		return ManagementFactory.getMemoryPoolMXBeans();
	}

	@Override
	ObjectName getObjectName(String managementDomain, MemoryPoolMXBean bean) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(String.format(OBJECT_NAME_PATTERN, managementDomain, getName(bean)));
	}

	@Override
	String getName(MemoryPoolMXBean bean) {
		return bean.getName();
	}
}
