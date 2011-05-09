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

import org.osgi.framework.Bundle;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.RequiredBundleHolder;
import org.eclipse.virgo.apps.admin.core.ServiceHolder;
import org.eclipse.virgo.kernel.module.Component;
import org.eclipse.virgo.kernel.module.ModuleContext;
import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
import org.eclipse.virgo.kernel.module.NoSuchComponentException;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.state.StateService;

/**
 * <p>
 * StandardArtifactHolder is the standard implementation of BundleHolder. It represents a 
 * bundle artifact and will provide formatting friendly strings where possible.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardArtifactHolder is thread-safe
 *
 */
final class StandardBundleHolder implements BundleHolder {

    private final ModuleContextAccessor moduleContextAccessor;

    private final QuasiBundle quasiBundle;

    private final StateService stateService;

    public StandardBundleHolder(QuasiBundle bundle, ModuleContextAccessor moduleContextAccessor, StateService stateService) {
        if(bundle == null || moduleContextAccessor == null) {
            throw new IllegalArgumentException("StandardBundleHolder must be provided with non-null QuasiBundle and ModuleContextAccessor.");
        }
        this.quasiBundle = bundle;
        this.moduleContextAccessor = moduleContextAccessor;
        this.stateService = stateService;
    }

    /** 
     * {@inheritDoc}
     */
    public Long getBundleId() {
        return this.quasiBundle.getBundleId();
    }

    /** 
     * {@inheritDoc}
     */
    public String getRegion(){
        return this.stateService.getBundleRegionName(getBundleId());
    }

    /** 
     * {@inheritDoc}
     */
    public String getSymbolicName() {
        return quasiBundle.getSymbolicName();
    }

    /** 
     * {@inheritDoc}
     */
    public String getState() {
        if(this.quasiBundle instanceof QuasiLiveBundle) {
            QuasiLiveBundle quasiLiveBundle = (QuasiLiveBundle) this.quasiBundle;
            return quasiLiveBundle.getState();
        }
        return this.quasiBundle.isResolved() ? "Resolved" : "Unresolved";
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return this.quasiBundle.isResolved();
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getVersion() {
        return quasiBundle.getVersion().toString();
    }
    
    /**
     * {@inheritDoc}
     */
    public Bundle getRawBundle() {
        return this.quasiBundle.getBundle();
    }

    /** 
     * {@inheritDoc}
     */
    public String getBundleLocation() {
        Bundle bundle2 = this.quasiBundle.getBundle();
        if(bundle2 != null) {
            return bundle2.getLocation();
        }
        return "";
    }

    /** 
     * {@inheritDoc}
     */
    public String getSpringName() {
        Bundle realBundle = quasiBundle.getBundle();
        if(realBundle != null) {
            ModuleContext moduleContext = this.moduleContextAccessor.getModuleContext(realBundle);
            if(moduleContext != null) {
                return moduleContext.getDisplayName();
            }
        }
        return "";
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<Component> getBeans(){
        List<Component> beans = new ArrayList<Component>();
        Bundle realBundle = quasiBundle.getBundle();
        if(realBundle != null) {
            ModuleContext moduleContext = this.moduleContextAccessor.getModuleContext(realBundle);
            if(moduleContext != null) {
                for(String name : moduleContext.getComponentNames()) {
                    try {
                        beans.add(moduleContext.getComponent(name));
                    } catch (NoSuchComponentException e) {
                        // no-op it is possible the bean has been removed since getting the list of names, thread safety.
                    }
                }
            }
        }
        return beans;
    }

    /** 
     * {@inheritDoc}
     */
    public List<ExportedPackageHolder> getExportPackages() {
        List<ExportedPackageHolder> exportedPackageHolders = new ArrayList<ExportedPackageHolder>();
        List<QuasiExportPackage> exportPackages = quasiBundle.getExportPackages();
        for(QuasiExportPackage exportPackage : exportPackages) {
            exportedPackageHolders.add(new StandardExportedPackageHolder(exportPackage, this.moduleContextAccessor, this.stateService));
        }
        return exportedPackageHolders;
    }

    /** 
     * {@inheritDoc}
     */
    public List<ImportedPackageHolder> getImportPackages() {
        List<ImportedPackageHolder> importedPackageHolders = new ArrayList<ImportedPackageHolder>();
        List<QuasiImportPackage> importPackages = quasiBundle.getImportPackages();
        for(QuasiImportPackage importPackage : importPackages) {
            importedPackageHolders.add(new StandardImportedPackageHolder(importPackage, this.moduleContextAccessor, this.stateService));
        }
        return importedPackageHolders;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<RequiredBundleHolder> getRequiredBundles(){
        List<QuasiRequiredBundle> quasiRequiredBundles = this.quasiBundle.getRequiredBundles();
        List<RequiredBundleHolder> requiredBundleHolders = new ArrayList<RequiredBundleHolder>();
        if(quasiRequiredBundles != null) {
            for(QuasiRequiredBundle quasiRequiredBundle : quasiRequiredBundles) {
                requiredBundleHolders.add(new StandardRequiredBundleHolder(quasiRequiredBundle, this.moduleContextAccessor, this.stateService));
            }
        }
        return requiredBundleHolders;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<BundleHolder> getHosts() {
        List<BundleHolder> artifactHolders = new ArrayList<BundleHolder>();
        List<QuasiBundle> hosts = quasiBundle.getHosts();
        if(hosts != null) {
            for(QuasiBundle hostQuasiBundle : hosts) {
                artifactHolders.add(new StandardBundleHolder(hostQuasiBundle, this.moduleContextAccessor, this.stateService));
            }
        }
        return artifactHolders;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<BundleHolder> getFragments() {
        List<BundleHolder> artifactHolders = new ArrayList<BundleHolder>();
        List<QuasiBundle> fragments = quasiBundle.getFragments();
        if(fragments != null) {
            for(QuasiBundle fragmentQuasiBundle : fragments) {
                artifactHolders.add(new StandardBundleHolder(fragmentQuasiBundle, this.moduleContextAccessor, this.stateService));
            }
        }
        return artifactHolders;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<ServiceHolder> getExportedServices(){
        List<ServiceHolder> serviceHolders = new ArrayList<ServiceHolder>();
        if(this.quasiBundle instanceof QuasiLiveBundle) {
            QuasiLiveBundle quasiLiveBundle = (QuasiLiveBundle) this.quasiBundle;
            List<QuasiLiveService> exportedServices = quasiLiveBundle.getExportedServices();
            for(QuasiLiveService quasiLiveService : exportedServices) {
                serviceHolders.add(new StandardServiceHolder(quasiLiveService, this.moduleContextAccessor, this.stateService));
            }
        }
        return serviceHolders;
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<ServiceHolder> getImportedServices(){
        List<ServiceHolder> serviceHolders = new ArrayList<ServiceHolder>();
        if(this.quasiBundle instanceof QuasiLiveBundle) {
            QuasiLiveBundle quasiLiveBundle = (QuasiLiveBundle) this.quasiBundle;
            List<QuasiLiveService> importedServices = quasiLiveBundle.getImportedServices();
            for(QuasiLiveService quasiLiveService : importedServices) {
                serviceHolders.add(new StandardServiceHolder(quasiLiveService, this.moduleContextAccessor, this.stateService));
            }
        }
        return serviceHolders;
    }

    public int compareTo(BundleHolder o) {
        return getBundleId().compareTo(o.getBundleId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) this.quasiBundle.getBundleId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StandardBundleHolder other = (StandardBundleHolder) obj;
        if (quasiBundle.getBundleId() != other.quasiBundle.getBundleId()) {
            return false;
        }
        return true;
    }

}
