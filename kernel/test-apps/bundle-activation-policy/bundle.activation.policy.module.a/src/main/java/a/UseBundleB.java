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

package a;

import b.ClassInBundleB;

public class UseBundleB {
	private final ClassInBundleB classInBundleB;
    public UseBundleB() {
		this.classInBundleB = new ClassInBundleB();
	}
	public ClassInBundleB getClassInBundleB() {
		return this.classInBundleB;
	}
}
