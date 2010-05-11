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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * An implementation that exports the Garbage Collector beans.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe.
 *
 */
public class GarbageCollectorSystemManagementExporter extends AbstractMultiBeanSystemManagementExporter<GarbageCollectorMXBean> {

	private static final String OBJECT_NAME_PATTERN = "%s:category=System Information,type=Garbage Collector,name=%s";

	@Override
	List<GarbageCollectorMXBean> getBeans() {
		return ManagementFactory.getGarbageCollectorMXBeans();
	}

	@Override
	ObjectName getObjectName(String managementDomain, GarbageCollectorMXBean bean) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(String.format(OBJECT_NAME_PATTERN, managementDomain, getName(bean)));
	}

	@Override
	String getName(GarbageCollectorMXBean bean) {
		return bean.getName();
	}

}
