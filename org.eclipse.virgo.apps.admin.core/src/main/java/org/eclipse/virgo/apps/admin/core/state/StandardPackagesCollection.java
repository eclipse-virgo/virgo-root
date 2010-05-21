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

package org.eclipse.virgo.apps.admin.core.state;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.PackagesCollection;



/**
 * <p>
 * StandardPackagesCollection is the an implementation of {@link PackagesCollection}.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardPackagesCollection is threadsafe
 *
 */
final class StandardPackagesCollection implements PackagesCollection {

    private final List<ExportedPackageHolder> exported;

    private final List<ImportedPackageHolder> imported;
    
    private final String packageName;

    public StandardPackagesCollection(String packageName, List<ImportedPackageHolder> imported, List<ExportedPackageHolder> exported) {
        if(packageName == null) {
            throw new IllegalArgumentException("Package name must be specified for a collection of packages");
        }
        this.packageName = packageName;
        this.imported = imported == null ? new ArrayList<ImportedPackageHolder>() : imported;
        this.exported = exported == null ? new ArrayList<ExportedPackageHolder>() : exported;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<ExportedPackageHolder> getExported() {
        return this.exported;
    }

    /** 
     * {@inheritDoc}
     */
    public List<ImportedPackageHolder> getImported() {
        return this.imported;
    }

    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.packageName;
    }

}
