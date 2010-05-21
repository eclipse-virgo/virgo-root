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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.PackagesCollection;


/**
 */
public class StubPackagesCollection implements PackagesCollection {

    private String packageName;

    public StubPackagesCollection(String packageName) {
        this.packageName = packageName;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<ExportedPackageHolder> getExported() {
        return new ArrayList<ExportedPackageHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public List<ImportedPackageHolder> getImported() {
        return new ArrayList<ImportedPackageHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.packageName;
    }

}
