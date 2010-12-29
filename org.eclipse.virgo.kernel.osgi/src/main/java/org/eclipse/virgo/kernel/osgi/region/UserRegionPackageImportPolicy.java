/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/


package org.eclipse.virgo.kernel.osgi.region;

import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;


/**
 * {@link UserRegionPackageImportPolicy} is a {@link RegionPackageImportPolicy} for a user region.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread safe.
 *
 */
class UserRegionPackageImportPolicy implements RegionPackageImportPolicy {
    
    private final List<ImportedPackage> importedPackages;
    
    UserRegionPackageImportPolicy(String regionImports) {
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setHeader("Import-Package", regionImports);
        this.importedPackages = manifest.getImportPackage().getImportedPackages();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isImported(String packageName) {
        //TODO support wildcards
        for (ImportedPackage importedPackage : this.importedPackages) {
            if (importedPackage.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}
