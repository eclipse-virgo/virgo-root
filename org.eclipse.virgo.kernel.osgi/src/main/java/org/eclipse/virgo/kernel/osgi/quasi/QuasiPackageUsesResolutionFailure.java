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

package org.eclipse.virgo.kernel.osgi.quasi;

/**
 * {@link QuasiPackageUsesResolutionFailure} describes a failure to resolve a package import of a {@link QuasiBundle} in
 * a {@link QuasiFramework} because of a user constraint.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class are thread safe.
 * 
 */
public interface QuasiPackageUsesResolutionFailure extends QuasiPackageResolutionFailure {

}
