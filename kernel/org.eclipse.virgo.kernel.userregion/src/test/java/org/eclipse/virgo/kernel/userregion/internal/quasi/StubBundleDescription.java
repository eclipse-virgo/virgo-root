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
import java.util.Map;

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
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

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
    @Override
    public boolean attachFragments() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dynamicFragments() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getBundleId() {
        return this.bid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State getContainingState() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleDescription[] getDependents() {
        return this.dependents.toArray(new BundleDescription[this.dependents.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getExecutionEnvironments() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExportPackageDescription[] getExportPackages() {
        return this.epds.toArray(new ExportPackageDescription[this.epds.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleDescription[] getFragments() {
        return this.fragments.toArray(new BundleDescription[this.fragments.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDescription[] getGenericCapabilities() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericSpecification[] getGenericRequires() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImportPackageSpecification[] getImportPackages() {
        return this.ipss.toArray(new ImportPackageSpecification[this.ipss.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocation() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NativeCodeSpecification getNativeCodeSpecification() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatformFilter() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleSpecification[] getRequiredBundles() {
        return this.rbs.toArray(new BundleSpecification[this.rbs.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExportPackageDescription[] getResolvedImports() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleDescription[] getResolvedRequires() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExportPackageDescription[] getSelectedExports() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExportPackageDescription[] getSubstitutedExports() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSymbolicName() {
        return this.bsn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getUserObject() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDynamicImports() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRemovalPending() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResolved() {
        return this.resolved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserObject(Object userObject) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleDescription getSupplier() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getDeclaredDirectives() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getDeclaredAttributes() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BundleCapability> getDeclaredCapabilities(String namespace) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypes() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImportPackageSpecification[] getAddedDynamicImportPackages() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDescription[] getSelectedGenericCapabilities() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDescription[] getResolvedGenericRequires() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleCapability getCapability() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BundleRequirement> getDeclaredRequirements(String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BundleWiring getWiring() {
        throw new UnsupportedOperationException();
    }

	@Override
	public List<Capability> getCapabilities(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Requirement> getRequirements(String arg0) {
		throw new UnsupportedOperationException();
	}

}
