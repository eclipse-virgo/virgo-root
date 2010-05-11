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

import org.eclipse.osgi.service.resolver.BaseDescription;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.GenericDescription;
import org.eclipse.osgi.service.resolver.GenericSpecification;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.NativeCodeSpecification;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;

/**
 */
public class StubBundleDescription implements BundleDescription {

    private String bsn;

    private Version bv;

    private boolean resolved;

    private long bid;

    private List<BundleDescription> fragments = new ArrayList<BundleDescription>();

    private List<BundleDescription> hosts = null;

    private List<ExportPackageDescription> epds = new ArrayList<ExportPackageDescription>();

    private List<ImportPackageSpecification> ipss = new ArrayList<ImportPackageSpecification>();

    private List<BundleSpecification> rbs = new ArrayList<BundleSpecification>();

    private List<BundleDescription> dependents = new ArrayList<BundleDescription>();

    public StubBundleDescription() {
    }

    public StubBundleDescription(String bsn) {
        this.bsn = bsn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean attachFragments() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean dynamicFragments() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public long getBundleId() {
        return this.bid;
    }

    /**
     * {@inheritDoc}
     */
    public State getContainingState() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public BundleDescription[] getDependents() {
        return this.dependents.toArray(new BundleDescription[this.dependents.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getExecutionEnvironments() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public ExportPackageDescription[] getExportPackages() {
        return this.epds.toArray(new ExportPackageDescription[this.epds.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public BundleDescription[] getFragments() {
        return this.fragments.toArray(new BundleDescription[this.fragments.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public GenericDescription[] getGenericCapabilities() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public GenericSpecification[] getGenericRequires() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public HostSpecification getHost() {
        return StubBundleDescription.this.hosts == null ? null : new HostSpecification() {

            public BundleDescription[] getHosts() {
                return StubBundleDescription.this.hosts.toArray(new BundleDescription[StubBundleDescription.this.hosts.size()]);
            }

            public boolean isMultiHost() {
                throw new UnsupportedOperationException();
            }

            public BundleDescription getBundle() {
                throw new UnsupportedOperationException();
            }

            public String getName() {
                throw new UnsupportedOperationException();
            }

            public BaseDescription getSupplier() {
                throw new UnsupportedOperationException();
            }

            public VersionRange getVersionRange() {
                throw new UnsupportedOperationException();
            }

            public boolean isResolved() {
                throw new UnsupportedOperationException();
            }

            public boolean isSatisfiedBy(BaseDescription supplier) {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * {@inheritDoc}
     */
    public ImportPackageSpecification[] getImportPackages() {
        return this.ipss.toArray(new ImportPackageSpecification[this.ipss.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public String getLocation() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public NativeCodeSpecification getNativeCodeSpecification() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getPlatformFilter() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public BundleSpecification[] getRequiredBundles() {
        return this.rbs.toArray(new BundleSpecification[this.rbs.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public ExportPackageDescription[] getResolvedImports() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public BundleDescription[] getResolvedRequires() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public ExportPackageDescription[] getSelectedExports() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public ExportPackageDescription[] getSubstitutedExports() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getSymbolicName() {
        return this.bsn;
    }

    /**
     * {@inheritDoc}
     */
    public Object getUserObject() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasDynamicImports() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRemovalPending() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return this.resolved;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSingleton() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void setUserObject(Object userObject) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public BundleDescription getSupplier() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Version getVersion() {
        return this.bv;
    }

    public void setBundleSymbolicName(String bsn) {
        this.bsn = bsn;
    }

    public void setVersion(Version bv) {
        this.bv = bv;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public void setBundleId(long bid) {
        this.bid = bid;
    }

    public void addFragment(BundleDescription f) {
        this.fragments.add(f);
    }

    public void addHost(StubBundleDescription bd) {
        if (this.hosts == null) {
            this.hosts = new ArrayList<BundleDescription>();
        }
        this.hosts.add(bd);
    }

    public void addExportPackage(ExportPackageDescription epd) {
        this.epds.add(epd);
    }

    public void addImportPackage(ImportPackageSpecification ips) {
        this.ipss.add(ips);
    }

    public void addRequiredBundle(BundleSpecification bs) {
        this.rbs.add(bs);
    }

    public void addDependent(BundleDescription d) {
        this.dependents.add(d);
    }

}
