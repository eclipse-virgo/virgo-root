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
import java.util.Map;

import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;

import org.eclipse.virgo.nano.serviceability.Assert;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;

/**
 * {@link BundleTrackedPackageImports} provides merging and tracking for package imports originating in bundle
 * manifests.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class BundleTrackedPackageImports extends AbstractTrackedPackageImports {

    private final BundleManifest bundleManifest;

    /**
     * Construct a {@link TrackedPackageImports} sourced from a bundle manifest.
     * 
     * @param importPackageHeader
     */
    BundleTrackedPackageImports(BundleManifest bundleManifest) {
        super(getInitialImportedPackages(bundleManifest));
        this.bundleManifest = bundleManifest;
    }

    /**
     * Extract a map of package name to {@link ImportedPackage} from the given bundle manifest.
     * <p />
     * Each <code>ImportedPackage</code> in the map contains a single package name.
     * 
     * @param bundleManifest the bundle manifest containing the package imports
     * @return a map of package name to {@link ImportedPackage}
     */
    private static Map<String, ImportedPackage> getInitialImportedPackages(BundleManifest bundleManifest) {
        Assert.notNull(bundleManifest, "bundleManifest must be non-null");
        List<ImportedPackage> importedPackages = bundleManifest.getImportPackage().getImportedPackages();
        return convertImportedPackageListToMap(importedPackages);
    }

    /**
     * {@inheritDoc}
     */
    public String getSource(String pkg) {
        Assert.notNull(this.bundleManifest.getBundleSymbolicName(), "bundleManifest must have a symbolic name");
        return !getInitialImportedPackages(this.bundleManifest).containsKey(pkg) ? null
            : ((this.bundleManifest.getFragmentHost().getBundleSymbolicName() == null ? "bundle " : "fragment ") + this.bundleManifest.getBundleSymbolicName().getSymbolicName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void merge(TrackedPackageImports importsToMerge) throws ImportMergeException {
        super.merge(importsToMerge);
    }

}
