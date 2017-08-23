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

import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;

/**
 * 
 *
 */
public class JMXQuasiResolutionFailure {

	private final QuasiResolutionFailure quasiResolutionFailure;

	public JMXQuasiResolutionFailure(QuasiResolutionFailure quasiResolutionFailure) {
		this.quasiResolutionFailure = quasiResolutionFailure;
	}
	
	public long getIdentifier(){
		return this.quasiResolutionFailure.getUnresolvedQuasiBundle().getBundleId();
	}
	
	public String getSymbolicName(){
		return this.quasiResolutionFailure.getUnresolvedQuasiBundle().getSymbolicName();
	}
	
	public String getVersion(){
		return this.quasiResolutionFailure.getUnresolvedQuasiBundle().getVersion().toString();
	}
	
	public String getDescription(){
		return this.quasiResolutionFailure.getDescription();
	}

}
