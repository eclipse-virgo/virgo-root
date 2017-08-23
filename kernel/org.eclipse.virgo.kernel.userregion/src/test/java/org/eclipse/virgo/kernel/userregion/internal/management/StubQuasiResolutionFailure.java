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
package org.eclipse.virgo.kernel.userregion.internal.management;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiParameterised;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;

/**
 * 
 *
 */
public class StubQuasiResolutionFailure implements QuasiResolutionFailure, QuasiParameterised {

	private final QuasiBundle quasiBundle;

	public StubQuasiResolutionFailure(QuasiBundle quasiBundle) {
		this.quasiBundle = quasiBundle;
	}
	
	@Override
	public Map<String, Object> getDirectives() {
		return new HashMap<String, Object>();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return new HashMap<String, Object>();
	}

	@Override
	public String getDescription() {
		return "Description";
	}

	@Override
	public QuasiBundle getUnresolvedQuasiBundle() {
		return this.quasiBundle;
	}

}
