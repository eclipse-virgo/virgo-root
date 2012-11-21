/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.kernel.userregion.internal.management;

import javax.management.MXBean;

/**
 * 
 * Implementations should be thread safe
 *
 */
@MXBean
public interface StateDumpMXBean {

	public JMXQuasiResolutionFailure[] getUnresolvedBundleFailures(String dumpFile);

	public JMXQuasiMinimalBundle[] listBundles(String dumpFile);
	
	public JMXQuasiBundle getBundle(String dumpFile, long bundleId);
	
}
