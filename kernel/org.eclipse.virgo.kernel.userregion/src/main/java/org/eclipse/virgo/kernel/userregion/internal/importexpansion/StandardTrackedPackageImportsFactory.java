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

package org.eclipse.virgo.kernel.userregion.internal.importexpansion;

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;

/**
 * {@link StandardTrackedPackageImportsFactory} provides a standard way of creating {@link TrackedPackageImports}
 * instances.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class StandardTrackedPackageImportsFactory implements TrackedPackageImportsFactory {

    private static final EmptyTrackedPackageImports EMPTY_TRACKED_PACKAGE_IMPORTS = new EmptyTrackedPackageImports();

    /**
     * {@inheritDoc}
     */
    public TrackedPackageImports create(BundleManifest bundleManifest) {
        return new BundleTrackedPackageImports(bundleManifest);
    }

    /**
     * {@inheritDoc}
     */
    public TrackedPackageImports create(List<ImportedPackage> importedPackages, String source) {
        return new AdditionalTrackedPackageImports(importedPackages, source);
    }

    /**
     * {@inheritDoc}
     */
    public TrackedPackageImports createCollector() {
        return new CollectingTrackedPackageImports();
    }

    /**
     * {@inheritDoc}
     */
    public TrackedPackageImports createContainer(String containingSource) {
        return new ContainingTrackedPackageImports(containingSource);
    }

    /**
     * {@inheritDoc}
     */
    public TrackedPackageImports createEmpty() {
        return EMPTY_TRACKED_PACKAGE_IMPORTS;
    }

}
