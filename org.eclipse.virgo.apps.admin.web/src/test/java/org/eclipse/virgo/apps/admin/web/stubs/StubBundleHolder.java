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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.module.Component;
import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;
import org.eclipse.virgo.apps.admin.core.RequiredBundleHolder;
import org.eclipse.virgo.apps.admin.core.ServiceHolder;


/**
 */
public class StubBundleHolder implements BundleHolder {

    /** 
     * {@inheritDoc}
     */
    public List<Component> getBeans() {
        return new ArrayList<Component>();
    }

    /** 
     * {@inheritDoc}
     */
    public Long getBundleId() {
        return 0l;
    }

    /** 
     * {@inheritDoc}
     */
    public String getBundleLocation() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getRegion() {
        return "";
    }
    
    /** 
     * {@inheritDoc}
     */
    public List<ExportedPackageHolder> getExportPackages() {
        return new ArrayList<ExportedPackageHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public List<ServiceHolder> getExportedServices() {
        return new ArrayList<ServiceHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public List<BundleHolder> getFragments() {
        return new ArrayList<BundleHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public List<BundleHolder> getHosts() {
        return new ArrayList<BundleHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public List<ImportedPackageHolder> getImportPackages() {
        return new ArrayList<ImportedPackageHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public List<ServiceHolder> getImportedServices() {
        return new ArrayList<ServiceHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public List<RequiredBundleHolder> getRequiredBundles() {
        return new ArrayList<RequiredBundleHolder>();
    }

    /** 
     * {@inheritDoc}
     */
    public String getSpringName() {
        return "";
    }

    /** 
     * {@inheritDoc}
     */
    public String getState() {
        return "";
    }

    /** 
     * {@inheritDoc}
     */
    public String getSymbolicName() {
        return "fake.test.bundle";
    }

    /** 
     * {@inheritDoc}
     */
    public String getVersion() {
        return new Version(1, 2, 3).toString();
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isResolved() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getRawBundle() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(BundleHolder o) {
        return this.getBundleId().compareTo(o.getBundleId());
    }

}
