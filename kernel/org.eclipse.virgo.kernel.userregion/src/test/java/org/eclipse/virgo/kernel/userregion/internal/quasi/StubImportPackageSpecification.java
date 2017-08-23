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

import org.eclipse.osgi.service.resolver.BaseDescription;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.wiring.BundleRequirement;

/**
 */
public class StubImportPackageSpecification extends StubParameterised implements ImportPackageSpecification {

    private String name;
    
    private VersionRange versionRange;

    private boolean resolved = false;

    private BaseDescription supplier;

    private BundleDescription bundle;

    public StubImportPackageSpecification(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleSymbolicName() {
        return "supplierBundle";
    }

    /**
     * {@inheritDoc}
     */
    public VersionRange getBundleVersionRange() {
        return new VersionRange("[2.0.0, 4.0.0)");
    }

    /**
     * {@inheritDoc}
     */
    public BundleDescription getBundle() {
        return this.bundle;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public BaseDescription getSupplier() {
        return this.supplier;
    }

    /**
     * {@inheritDoc}
     */
    public VersionRange getVersionRange() {
        return this.versionRange;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return this.resolved ;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSatisfiedBy(BaseDescription supplier) {
        return this.name.equals(supplier.getName());
    }
    
    public void setVersionRange(VersionRange versionRange) {
        this.versionRange = versionRange;
    }

    public void setResolved(boolean b) {
        this.resolved = b;
    }
    
    public void setBundle(BundleDescription bundle) {
        this.bundle = bundle;
    }
    
    public void setSupplier(BaseDescription supplier) {
        this.supplier = supplier;
    }

    @Override
    public BundleRequirement getRequirement() {
        throw new UnsupportedOperationException();
    }

	@Override
	public Object getUserObject() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUserObject(Object arg0) {
		throw new UnsupportedOperationException();
	}

}
