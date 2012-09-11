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

package org.eclipse.virgo.shell.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.util.common.StringUtils;


/**
 * <p>
 * QuasiPackageUtil acts as a simple data holder
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * QuasiPackageUtil is immutable.
 *
 */
public final class PackageHolder {

    private final String packageName;
    
    private final List<QuasiImportPackage> quasiImportPackages;
    
    private final List<QuasiExportPackage> quasiExportPackages;

    public PackageHolder(List<QuasiExportPackage> quasiExportPackages, List<QuasiImportPackage> quasiImportPackages, String packageName) {
        if(!StringUtils.hasLength(packageName)){
            throw new IllegalArgumentException("QuasiPackageName must not be null and have at least one character");
        }
        this.quasiExportPackages = quasiExportPackages == null ? new ArrayList<QuasiExportPackage>() : quasiExportPackages;
        this.quasiImportPackages = quasiImportPackages == null ? new ArrayList<QuasiImportPackage>() : quasiImportPackages;
        this.packageName = packageName;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<QuasiExportPackage> getExporters() {
        return this.quasiExportPackages;
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiImportPackage> getImporters() {
        return this.quasiImportPackages;
    }

    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.packageName;
    }

}
