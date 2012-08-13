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

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;

/**
 * 
 *
 */
public class JMXQuasiMinimalBundle {

	private final QuasiBundle quasiBundle;

	public JMXQuasiMinimalBundle(QuasiBundle quasiBundle) {
		this.quasiBundle = quasiBundle;
	}

	public final long getIdentifier(){
		return this.quasiBundle.getBundleId();
	}
	
	public final String getSymbolicName(){
		return this.quasiBundle.getSymbolicName();
	}
	
	public final String getVersion() {
		return this.quasiBundle.getVersion().toString();
	}
}
