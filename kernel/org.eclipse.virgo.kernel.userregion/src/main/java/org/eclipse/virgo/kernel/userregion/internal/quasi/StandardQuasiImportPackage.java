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

import static org.osgi.framework.Constants.RESOLUTION_DIRECTIVE;
import static org.osgi.framework.Constants.RESOLUTION_MANDATORY;
import static org.osgi.framework.Constants.RESOLUTION_OPTIONAL;

import org.eclipse.osgi.service.resolver.BaseDescription;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * {@link StandardQuasiImportPackage} is the default implementation of {@link QuasiImportPackage}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public class StandardQuasiImportPackage extends StandardQuasiParameterised implements QuasiImportPackage {

    private final ImportPackageSpecification importPackageSpecification;

    private final QuasiBundle importingBundle;

    public StandardQuasiImportPackage(ImportPackageSpecification importPackageSpecification, QuasiBundle importingBundle) {
        super(importPackageSpecification);
        this.importPackageSpecification = importPackageSpecification;
        this.importingBundle = importingBundle;
    }

    /**
     * {@inheritDoc}
     */
    public String getPackageName() {
        return this.importPackageSpecification.getName();
    }

    /**
     * {@inheritDoc}
     */
    public VersionRange getVersionConstraint() {
        org.eclipse.osgi.service.resolver.VersionRange resolverVersionRange = this.importPackageSpecification.getVersionRange();
        VersionRange versionRange;
        if (resolverVersionRange == null) {
            versionRange = new VersionRange(null); // The range of all possible versions
        } else {
            versionRange = new VersionRange(resolverVersionRange.toString());
        }
        return versionRange;
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getImportingBundle() {
        return this.importingBundle;
    }

    public boolean isOptional() {
        Object resolutionObject = this.importPackageSpecification.getDirective(RESOLUTION_DIRECTIVE);
        if (resolutionObject == null) {
            resolutionObject = RESOLUTION_MANDATORY;
        }
        return resolutionObject.equals(RESOLUTION_OPTIONAL);
    }

    /**
     * {@inheritDoc}
     */
    public QuasiExportPackage getProvider() {
        QuasiExportPackage provider = null;
        if (isResolved()) {
            ExportPackageDescription providerDescription = getProviderDescription();
            if (providerDescription != null) {
                provider = constructProvider(providerDescription);
            }
        }
        return provider;
    }

    private QuasiExportPackage constructProvider(ExportPackageDescription providerDescription) {
        BundleDescription exporter = providerDescription.getExporter();
		StandardQuasiBundle quasiExporter = new StandardQuasiBundle(exporter, null, this.importingBundle.getRegion().getRegionDigraph().getRegion(exporter.getBundleId()));
        return new StandardQuasiExportPackage(providerDescription, quasiExporter);
    }

    private ExportPackageDescription getProviderDescription() {        
        BaseDescription supplier = this.importPackageSpecification.getSupplier();
        if (supplier instanceof ExportPackageDescription) {
            return (ExportPackageDescription) supplier;
        }
        
        return null;
    }    

    /**
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return this.importPackageSpecification.isResolved();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "StandardQuasiImportPackage(" + getPackageName() + ", " + getVersionConstraint().toString() + ", " + super.toString() + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((importPackageSpecification == null) ? 0 : importPackageSpecification.hashCode());
        result = prime * result + ((importingBundle == null) ? 0 : importingBundle.hashCode());
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
        StandardQuasiImportPackage other = (StandardQuasiImportPackage) obj;
        if (importPackageSpecification == null) {
            if (other.importPackageSpecification != null)
                return false;
        } else if (!importPackageSpecification.equals(other.importPackageSpecification))
            return false;
        if (importingBundle == null) {
            if (other.importingBundle != null)
                return false;
        } else if (!importingBundle.equals(other.importingBundle))
            return false;
        return true;
    }

}
