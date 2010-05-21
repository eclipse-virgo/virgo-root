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

package org.eclipse.virgo.apps.admin.core.stubs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.shell.state.QuasiPackage;


/**
 */
public class StubQuasiPackages implements QuasiPackage {

    private String packageName;

    public StubQuasiPackages(String packageName) {
        this.packageName = packageName;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<QuasiExportPackage> getExporters() {
        return new ArrayList<QuasiExportPackage>();
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiImportPackage> getImporters() {
        return new ArrayList<QuasiImportPackage>();
    }

    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.packageName;
    }

}
