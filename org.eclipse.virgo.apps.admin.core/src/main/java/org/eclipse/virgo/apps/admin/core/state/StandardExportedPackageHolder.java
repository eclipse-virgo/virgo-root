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
import java.util.Map;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.shell.state.StateService;

/**
 * <p>
 * StandardExportedPackageHolder is the standard implementation of {@link ExportedPackageHolder}.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardExportedPackageHolder is thread-safe
 *
 */
final class StandardExportedPackageHolder implements ExportedPackageHolder {

    private final QuasiExportPackage exportPackage;
    
    private final ModuleContextAccessor moduleContextAccessor;

    private final StateService stateService;

    public StandardExportedPackageHolder(QuasiExportPackage exportPackage, ModuleContextAccessor moduleContextAccessor, StateService stateService) {
        this.exportPackage = exportPackage;
        this.moduleContextAccessor = moduleContextAccessor;
        this.stateService = stateService;
    }

    /** 
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.exportPackage.getPackageName();
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<ImportedPackageHolder> getConsumers() {
        List<QuasiImportPackage> consumers = this.exportPackage.getConsumers();
        List<ImportedPackageHolder> importedPackageHolders = new ArrayList<ImportedPackageHolder>();
        if(consumers != null) {
            for(QuasiImportPackage quasiImportPackage : consumers) {
                importedPackageHolders.add(new StandardImportedPackageHolder(quasiImportPackage, this.moduleContextAccessor, this.stateService));
            }
        }
        return importedPackageHolders;
    }

    /** 
     * {@inheritDoc}
     */
    public BundleHolder getExportingBundle() {
        QuasiBundle exportingBundle = this.exportPackage.getExportingBundle();
        if(exportingBundle != null) {
            return new StandardBundleHolder(exportingBundle, this.moduleContextAccessor, this.stateService) ;
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public String getVersion() {
        return this.exportPackage.getVersion().toString();
    }


    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getAttributes() {
        return ObjectFormatter.formatMapValues(this.exportPackage.getAttributes());
    }

    /** 
     * {@inheritDoc}
     */
    public Map<String, String> getDirectives() {
        return ObjectFormatter.formatMapValues(this.exportPackage.getDirectives());
    }

}
