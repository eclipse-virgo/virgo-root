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

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiPackageResolutionFailure;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * <p>
 * TODO Document PackageQuasiResolutionFailure
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * TODO Document concurrent semantics of PackageQuasiResolutionFailure
 *
 */
class PackageQuasiResolutionFailure extends GenericQuasiResolutionFailure implements QuasiPackageResolutionFailure {

    private final String bundleSymbolicName;

    private final VersionRange bundleVersionRange;

    private final String pkg;

    private final VersionRange pkgVersionRange;

    public PackageQuasiResolutionFailure(String description, QuasiBundle quasiBundle, String pkg,
        VersionRange pkgVersionRange, String bundleSymbolicName, VersionRange bundleVersionRange) {
        super(quasiBundle, description);
        this.bundleSymbolicName = bundleSymbolicName;
        this.bundleVersionRange = bundleVersionRange;
        this.pkg = pkg;
        this.pkgVersionRange = pkgVersionRange;
    }

    public String getPackageBundleSymbolicName() {
        return this.bundleSymbolicName;
    }

    public VersionRange getPackageBundleVersionRange() {
        return this.bundleVersionRange;
    }

    public String getPackage() {
        return this.pkg;
    }

    public VersionRange getPackageVersionRange() {
        return this.pkgVersionRange;
    }

}
