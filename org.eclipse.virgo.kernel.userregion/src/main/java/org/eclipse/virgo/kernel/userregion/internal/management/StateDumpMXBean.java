/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
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

	/**
	 * Get a list of the unresolved BundleIds
	 * 
	 * @param dumpFile
	 * @return
	 */
	public long[] getUnresolvedBundleIds(String dumpFile);

	public BundleMXBean[] listBundles(String dumpFile);
	
	public BundleMXBean getBundle(String dumpFile, long bundleId);
	
}
