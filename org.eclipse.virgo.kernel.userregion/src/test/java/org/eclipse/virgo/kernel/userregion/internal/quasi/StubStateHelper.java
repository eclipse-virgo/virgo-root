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

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.StateHelper;
import org.eclipse.osgi.service.resolver.VersionConstraint;

/**
 */
public class StubStateHelper implements StateHelper {

    private BundleDescription[] dependentBundles;
    
    private ExportPackageDescription[] visiblePackages;

    /**
     * {@inheritDoc}
     */
    public int getAccessCode(BundleDescription bundle, ExportPackageDescription export) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public BundleDescription[] getDependentBundles(BundleDescription[] bundles) {
        return this.dependentBundles;

    }

    /**
     * {@inheritDoc}
     */
    public BundleDescription[] getPrerequisites(BundleDescription[] bundles) {
        throw new UnsupportedOperationException();

    }

    /**
     * {@inheritDoc}
     */
    public VersionConstraint[] getUnsatisfiedConstraints(BundleDescription bundle) {
        throw new UnsupportedOperationException();

    }

    /**
     * {@inheritDoc}
     */
    public VersionConstraint[] getUnsatisfiedLeaves(BundleDescription[] bundles) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public ExportPackageDescription[] getVisiblePackages(BundleDescription bundle) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public ExportPackageDescription[] getVisiblePackages(BundleDescription bundle, int options) {
        return this.visiblePackages;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResolvable(ImportPackageSpecification specification) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResolvable(BundleSpecification specification) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResolvable(HostSpecification specification) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Object[][] sortBundles(BundleDescription[] toSort) {
        throw new UnsupportedOperationException();
    }
    
    public void setDependentBundles(BundleDescription[] dependentBundles) {
        this.dependentBundles = dependentBundles;
    }
    
    public void setVisiblePackages(ExportPackageDescription[] visiblePackages) {
        this.visiblePackages = visiblePackages;
    }

}
