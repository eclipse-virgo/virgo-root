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

import java.util.Map;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.shell.state.StateService;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * <p>
 * StandardImportedPackageHolder is the standard implementation of {@link ImportedPackageHolder}.
 * It is backed by a {@link QuasiImportPackage}.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardImportedPackageHolder is thread-safe
 *
 */
final class StandardImportedPackageHolder implements ImportedPackageHolder {

    private final ModuleContextAccessor moduleContextAccessor;
    
    private final QuasiImportPackage importPackage;

    private final StateService stateService;
    
    public StandardImportedPackageHolder(QuasiImportPackage importPackage, ModuleContextAccessor moduleContextAccessor, StateService stateService) {
        this.importPackage = importPackage;
        this.moduleContextAccessor = moduleContextAccessor;
        this.stateService = stateService;
    }

    /** 
     * {@inheritDoc}
     */
    public BundleHolder getImportingBundle() {
        QuasiBundle importingBundle = this.importPackage.getImportingBundle();
        if(importingBundle != null) {
            return new StandardBundleHolder(importingBundle, this.moduleContextAccessor, this.stateService);
        } 
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.importPackage.getPackageName();
    }

    /** 
     * {@inheritDoc}
     */
    public String getVersionConstraint() {
        VersionRange versionConstraint = this.importPackage.getVersionConstraint();
        return versionConstraint.toString().replace("°", "&infin;");
    }

    /** 
     * {@inheritDoc}
     */
    public ExportedPackageHolder getProvider() {
        QuasiExportPackage provider = this.importPackage.getProvider();
        if(provider != null) {
            return new StandardExportedPackageHolder(provider, this.moduleContextAccessor, this.stateService);
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return this.importPackage.isResolved();
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getAttributes() {
        return ObjectFormatter.formatMapValues(this.importPackage.getAttributes());
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getDirectives() {
        return ObjectFormatter.formatMapValues(this.importPackage.getDirectives());
    }    

}
