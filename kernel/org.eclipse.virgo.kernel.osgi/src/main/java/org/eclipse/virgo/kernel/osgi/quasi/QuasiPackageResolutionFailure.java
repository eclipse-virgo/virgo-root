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

import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * {@link QuasiPackageResolutionFailure} describes a failure to resolve a package import of a {@link QuasiBundle} in a
 * {@link QuasiFramework}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this class are thread safe.
 * 
 */
public interface QuasiPackageResolutionFailure extends QuasiResolutionFailure {

    String getPackage();

    VersionRange getPackageVersionRange();

    String getPackageBundleSymbolicName();

    VersionRange getPackageBundleVersionRange();

}
