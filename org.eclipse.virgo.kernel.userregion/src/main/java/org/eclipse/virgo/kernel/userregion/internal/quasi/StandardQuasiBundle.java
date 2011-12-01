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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.StateHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.artifact.plan.PlanDescriptor.Provisioning;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * {@link StandardQuasiBundle} is the default implementation of {@link QuasiBundle}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class StandardQuasiBundle implements QuasiBundle {

    private final BundleDescription bundleDescription;

    private final BundleManifest bundleManifest;

    private volatile Bundle bundle;

    private final String bsn;

    private Version bv;

    private final StateHelper stateHelper;

    private volatile Provisioning provisioning = Provisioning.AUTO;

    /**
     * Constructs an unresolved, uncommitted {@link QuasiBundle} with the given {@link BundleDescription}.
     * 
     * @param bundleDescription the <code>BundleDescription</code> for this <code>QuasiBundle</code>
     * @param bundleManifest
     * @param stateHelper a {@link StateHelper} for analysing wiring
     */
    public StandardQuasiBundle(BundleDescription bundleDescription, BundleManifest bundleManifest, StateHelper stateHelper) {
        this.bundleDescription = bundleDescription;
        this.bundleManifest = bundleManifest;
        this.bsn = bundleDescription.getSymbolicName();
        this.bv = bundleDescription.getVersion();
        this.stateHelper = stateHelper;
    }

    BundleDescription getBundleDescription() {
        return this.bundleDescription;
    }

    BundleManifest getBundleManifest() {
        return this.bundleManifest;
    }

    /**
     * {@inheritDoc}
     */
    public String getSymbolicName() {
        return this.bundleDescription.getSymbolicName();
    }

    /**
     * {@inheritDoc}
     */
    public Version getVersion() {
        return this.bundleDescription.getVersion();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return this.bundleDescription.isResolved();
    }

    /**
     * {@inheritDoc}
     */
    public void uninstall() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getBundle() {
        return this.bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "QuasiBundle(" + getSymbolicName() + ", " + getVersion() + ")";
    }

    /**
     * {@inheritDoc}
     */
    public long getBundleId() {
        return this.bundleDescription.getBundleId();
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiBundle> getFragments() {
        BundleDescription[] fragments = this.bundleDescription.getFragments();
        return this.wrapBundleDescriptions(fragments);
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiBundle> getHosts() {
        HostSpecification hostSpecification = this.bundleDescription.getHost();
        return hostSpecification == null ? null : this.wrapBundleDescriptions(hostSpecification.getHosts());
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiExportPackage> getExportPackages() {
        ExportPackageDescription[] exportPackages = this.bundleDescription.getExportPackages();
        return this.wrapExportPackageDescriptions(exportPackages);
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiImportPackage> getImportPackages() {
        return this.wrapImportPackageSpecifications(this.bundleDescription.getImportPackages());
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiRequiredBundle> getRequiredBundles() {
        return this.wrapBundleSpecificationsAsRequiredBundles(this.bundleDescription.getRequiredBundles());
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiBundle> getDependents() {
        BundleDescription[] dependents = this.bundleDescription.getDependents();
        return this.wrapBundleDescriptions(dependents);
    }

    /**
     * Utility method to wrap a list of {@link BundleDescription} in QuasiBundle.
     * 
     * @param bundleDescriptions
     * @return
     */
    private List<QuasiBundle> wrapBundleDescriptions(BundleDescription[] bundleDescriptions) {
        List<QuasiBundle> quasiBundles = new ArrayList<QuasiBundle>();
        for (BundleDescription bundleDescription : bundleDescriptions) {
            quasiBundles.add(new StandardQuasiBundle(bundleDescription, null, this.stateHelper));
        }
        return Collections.unmodifiableList(quasiBundles);
    }

    /**
     * Utility method to wrap a list of {@link BundleDescription} in QuasiBundle.
     * 
     * @param bundleDescriptions
     * @return
     */
    private List<QuasiRequiredBundle> wrapBundleSpecificationsAsRequiredBundles(BundleSpecification[] bundleDescriptions) {
        List<QuasiRequiredBundle> quasiRequiredBundles = new ArrayList<QuasiRequiredBundle>();
        for (BundleSpecification bundleSpecification : bundleDescriptions) {
            quasiRequiredBundles.add(new StandardQuasiRequiredBundle(bundleSpecification, this));
        }
        return Collections.unmodifiableList(quasiRequiredBundles);
    }

    /**
     * Utility method to wrap a list of {@link BundleDescription} in {@link QuasiExportPackage}.
     * 
     * @param exportPackageDescriptions
     * @param quasiBundle
     * @return
     */
    private List<QuasiExportPackage> wrapExportPackageDescriptions(ExportPackageDescription[] exportPackageDescriptions) {
        List<QuasiExportPackage> quasiExportPackages = new ArrayList<QuasiExportPackage>();
        for (ExportPackageDescription exportPackageDescription : exportPackageDescriptions) {
            quasiExportPackages.add(new StandardQuasiExportPackage(exportPackageDescription, this));
        }
        return Collections.unmodifiableList(quasiExportPackages);
    }

    /**
     * Utility method to wrap a list of {@link BundleDescription} in {@link QuasiImportPackage}.
     * 
     * @param bundleDescriptions
     * @return
     */
    private List<QuasiImportPackage> wrapImportPackageSpecifications(ImportPackageSpecification[] importPackageSpecifications) {
        List<QuasiImportPackage> quasiImportPackages = new ArrayList<QuasiImportPackage>();
        for (ImportPackageSpecification importPackageSpecification : importPackageSpecifications) {
            quasiImportPackages.add(new StandardQuasiImportPackage(importPackageSpecification, this));
        }
        return Collections.unmodifiableList(quasiImportPackages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bsn == null) ? 0 : bsn.hashCode());
        result = prime * result + ((bv == null) ? 0 : bv.hashCode());
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
        StandardQuasiBundle other = (StandardQuasiBundle) obj;
        if (bsn == null) {
            if (other.bsn != null)
                return false;
        } else if (!bsn.equals(other.bsn))
            return false;
        if (bv == null) {
            if (other.bv != null)
                return false;
        } else if (!bv.equals(other.bv))
            return false;
        return true;
    }

    public StateHelper getStateHelper() {
        return this.stateHelper;
    }

    public File getBundleFile() {
        if (bundle instanceof BundleHost) {
            BundleHost bh = (BundleHost) bundle;
            BundleData bundleData = bh.getBundleData();
            if (bundleData instanceof BaseData) {
                File file = ((BaseData) bundleData).getBundleFile().getBaseFile();
                return file;
            }
        }
        return null;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void setProvisioning(Provisioning provisioning) {
        if (provisioning == null) {
            throw new IllegalArgumentException("null not a valid provisioning behaviour for a QuasiBundle");
        }
        if (provisioning == Provisioning.INHERIT) {
            throw new IllegalArgumentException("INHERIT is not a valid provisioning behaviour for a QuasiBundle");
        }
        this.provisioning = provisioning;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Provisioning getProvisioning() {
        return this.provisioning;
    }

}
