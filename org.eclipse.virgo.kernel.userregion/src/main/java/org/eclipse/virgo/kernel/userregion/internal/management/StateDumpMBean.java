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
public interface StateDumpMBean {

	/**
	 * Get the summary explanation of why the resolution failure occurred.
	 * 
	 * @param dumpFile
	 * @return
	 */
	public String[] getSummary(String dumpFile);
	
}
