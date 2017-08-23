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

import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;

/**
 * {@link AdditionalTrackedPackageImports} is a {@link TrackedPackageImports} representing a collection of package
 * imports to be added to one or more bundles from a given source.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
class AdditionalTrackedPackageImports extends AbstractTrackedPackageImports {

    private final List<ImportedPackage> importedPackages;

    private final String source;

    /**
     * Construct an {@link AdditionalTrackedPackageImports} with the given collection of package imports and the given
     * source.
     * 
     * @param importedPackages the list of package imports
     * @param source the source
     */
    AdditionalTrackedPackageImports(List<ImportedPackage> importedPackages, String source) {
        super(convertImportedPackageListToMap(importedPackages));
        this.importedPackages = importedPackages;
        this.source = source;
    }

    /**
     * {@inheritDoc}
     */
    public String getSource(String pkg) {
        return !convertImportedPackageListToMap(this.importedPackages).containsKey(pkg) ? null : this.source;
    }

}
