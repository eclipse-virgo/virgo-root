/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.management.console.stubs.types;

import org.mozilla.javascript.ScriptableObject;

/**
 * 
 * 
 */
public abstract class ParentStub extends ScriptableObject {

	private static final long serialVersionUID = 1L;

	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}
	
}
