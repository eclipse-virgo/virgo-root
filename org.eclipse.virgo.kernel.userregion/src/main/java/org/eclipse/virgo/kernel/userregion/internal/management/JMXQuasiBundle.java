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

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;

/**
 * 
 *
 */
public final class JMXQuasiBundle extends JMXQuasiMinimalBundle{

	private final QuasiBundle quasiBundle;

	protected JMXQuasiBundle(QuasiBundle quasiBundle) {
		super(quasiBundle);
		this.quasiBundle = quasiBundle;
	}
	
}
