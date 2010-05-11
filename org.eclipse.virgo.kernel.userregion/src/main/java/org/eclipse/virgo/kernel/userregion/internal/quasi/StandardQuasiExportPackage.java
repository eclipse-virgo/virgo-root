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

package org.eclipse.virgo.kernel.userregion.internal.quasi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.StateHelper;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;

/**
 * {@link StandardQuasiExportPackage} is the default implementation of {@link QuasiExportPackage}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class StandardQuasiExportPackage extends StandardQuasiParameterised implements QuasiExportPackage {

    private final ExportPackageDescription exportPackageDescription;

    private final QuasiBundle exporter;

    public StandardQuasiExportPackage(ExportPackageDescription exportPackageDescription, QuasiBundle exporter) {
        super(exportPackageDescription);
        this.exportPackageDescription = exportPackageDescription;
        this.exporter = exporter;
    }

    private StateHelper getStateHelper() {
        return ((StandardQuasiBundle) exporter).getStateHelper();
    }

    /**
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.exportPackageDescription.getName();
    }

    /**
     * {@inheritDoc}
     */
    public Version getVersion() {
        return this.exportPackageDescription.getVersion();
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getExportingBundle() {
        return this.exporter;
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiImportPackage> getConsumers() {
        List<QuasiImportPackage> consumers = new ArrayList<QuasiImportPackage>();
        StateHelper stateHelper = getStateHelper();
        for (BundleDescription dependentBundle : getDependentBundles(stateHelper)) {
            if (isConsumer(dependentBundle)) {
                addConsumer(dependentBundle, consumers, stateHelper);
            }
        }
        return consumers;
    }

    private BundleDescription[] getDependentBundles(StateHelper stateHelper) {
        return stateHelper.getDependentBundles(new BundleDescription[] { this.exportPackageDescription.getExporter() });
    }

    private boolean isConsumer(BundleDescription dependentBundle) {
        ImportPackageSpecification[] importedPackages = dependentBundle.getImportPackages();
        
        for (ImportPackageSpecification importedPackage : importedPackages) {
            if (this.exportPackageDescription.equals(importedPackage.getSupplier())) {
                return true;
            }
        }
        return false;
    }

    private void addConsumer(BundleDescription dependentBundle, List<QuasiImportPackage> consumers, StateHelper stateHelper) {
        ImportPackageSpecification[] dependentImportPackages = dependentBundle.getImportPackages();
        for (ImportPackageSpecification dependentImportPackage : dependentImportPackages) {
            if (matches(dependentImportPackage, this.exportPackageDescription)) {
                consumers.add(new StandardQuasiImportPackage(dependentImportPackage, new StandardQuasiBundle(dependentBundle, null,
                    stateHelper)));
                break;
            }
        }
    }

    private boolean matches(ImportPackageSpecification i, ExportPackageDescription e) {
        return i.isSatisfiedBy(e);
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "StandardQuasiExportPackage(" + getPackageName() + ", " + getVersion().toString() + ", " + super.toString() +")";
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((exportPackageDescription == null) ? 0 : exportPackageDescription.hashCode());
        result = prime * result + ((exporter == null) ? 0 : exporter.hashCode());
        return result;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StandardQuasiExportPackage other = (StandardQuasiExportPackage) obj;
        if (exportPackageDescription == null) {
            if (other.exportPackageDescription != null)
                return false;
        } else if (!exportPackageDescription.equals(other.exportPackageDescription))
            return false;
        if (exporter == null) {
            if (other.exporter != null)
                return false;
        } else if (!exporter.equals(other.exporter))
            return false;
        return true;
    }

}
