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

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

/**
 * An implementation that exports the ClassLoading bean.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Threadsafe.
 *
 */
public class ClassLoadingSystemManagementExporter extends AbstractSystemManagementExporter<ClassLoadingMXBean> {

	private static final String CLASS_LOADING = "Class Loading";

	@Override
	ClassLoadingMXBean getBean() {
		return ManagementFactory.getClassLoadingMXBean();
	}

	@Override
	String getName() {
		return CLASS_LOADING;
	}

}
