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

package org.eclipse.virgo.kernel.shell.state.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;


/**
 * <p>
 * StandardQuasiLiveBundle is the standard implementation of {@link QuasiLiveBundle}. In 
 * many cases it simply passes requests off to a wrapped {@link QuasiBundle} only adding 
 * the extra functionality required for a <code>QuasiLiveBundle</code>.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardQuasiLiveBundle is threadsafe
 *
 */
final class StandardQuasiLiveBundle implements QuasiLiveBundle {

    private final QuasiBundle quasiBundle;
    
    private final Bundle osgiBundle;

    private final QuasiFramework quasiFramework;

    public StandardQuasiLiveBundle(QuasiFramework quasiFramework, QuasiBundle quasiBundle, Bundle osgiBundle) {
        this.quasiFramework = quasiFramework;
        this.quasiBundle = quasiBundle;
        if(osgiBundle != null){
            this.osgiBundle = osgiBundle;
        } else {
            this.osgiBundle = new KernelRegionFauxQuasiLiveBundle(quasiBundle.getBundleId(), quasiBundle.getVersion(), quasiBundle.getSymbolicName());
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<QuasiLiveService> getExportedServices() {
        List<QuasiLiveService> quasiLiveServices = new ArrayList<QuasiLiveService>();
        ServiceReference<?>[] registeredServices = this.osgiBundle.getRegisteredServices();
        if(registeredServices == null){
            return quasiLiveServices;
        }
        for(ServiceReference<?> serviceReference : registeredServices){
            quasiLiveServices.add(new StandardQuasiLiveService(this.quasiFramework, serviceReference));
        }
        return quasiLiveServices;
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiLiveService> getImportedServices() {
        List<QuasiLiveService> quasiLiveServices = new ArrayList<QuasiLiveService>();
        ServiceReference<?>[] registeredServices = this.osgiBundle.getServicesInUse();
        if(registeredServices == null){
            return quasiLiveServices;
        }
        for(ServiceReference<?> serviceReference : registeredServices){
            quasiLiveServices.add(new StandardQuasiLiveService(this.quasiFramework, serviceReference));
        }
        return quasiLiveServices;
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getState() {
        int state = this.osgiBundle.getState();
        switch(state){
            case Bundle.UNINSTALLED : return "Uninstalled";
            case Bundle.INSTALLED : return "Installed";
            case Bundle.RESOLVED : return "Resolved";
            case Bundle.STARTING : return "Starting";
            case Bundle.STOPPING : return "Stopping";
            case Bundle.ACTIVE : return "Active";
            default : return "Unknown";
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Bundle getBundle() {
        return this.osgiBundle;
    }

    /** 
     * {@inheritDoc}
     */
    public long getBundleId() {
        return this.quasiBundle.getBundleId();
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiBundle> getDependents() {
        return this.quasiBundle.getDependents();
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiExportPackage> getExportPackages() {
        return this.quasiBundle.getExportPackages();
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiBundle> getFragments() {
        return this.quasiBundle.getFragments();
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiBundle> getHosts() {
        return this.quasiBundle.getHosts();
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiImportPackage> getImportPackages() {
        return this.quasiBundle.getImportPackages();
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiRequiredBundle> getRequiredBundles() {
        return this.quasiBundle.getRequiredBundles();
    }

    /** 
     * {@inheritDoc}
     */
    public String getSymbolicName() {
        return this.quasiBundle.getSymbolicName();
    }

    /** 
     * {@inheritDoc}
     */
    public Version getVersion() {
        return this.quasiBundle.getVersion();
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
    public void uninstall() {
        this.quasiBundle.uninstall();
    }

    /**
     * {@inheritDoc}
     */
    public File getBundleFile() {
        return this.quasiBundle.getBundleFile();
    }

}
