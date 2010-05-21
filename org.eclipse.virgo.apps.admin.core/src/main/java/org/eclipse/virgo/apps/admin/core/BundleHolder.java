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

package org.eclipse.virgo.apps.admin.core;

import java.util.List;

import org.osgi.framework.Bundle;

import org.eclipse.virgo.kernel.module.Component;

/**
 * <p>
 * BundleHolder represents a bundle artifact with Spring information contained within it.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 *  BundleHolder implementations should be thread safe
 *
 */
public interface BundleHolder extends Comparable<BundleHolder>{

    public Long getBundleId();

    public String getSymbolicName();

    public String getState();

    public boolean isResolved();

    public String getVersion();

    public String getBundleLocation();

    public String getSpringName();

    public List<Component> getBeans();

    public List<ExportedPackageHolder> getExportPackages();

    public List<ImportedPackageHolder> getImportPackages();

    public List<RequiredBundleHolder> getRequiredBundles();

    public List<BundleHolder> getHosts();

    public Bundle getRawBundle();
    
    public List<BundleHolder> getFragments();

    /**
     * A list of {@link ServiceHolder}s that are published by this {@link BundleHolder}
     * @return list of services
     */
    public List<ServiceHolder> getExportedServices();

    /**
     * A list of {@link ServiceHolder}s that are consumed by this {@link BundleHolder}
     * @return list of services
     */
    public List<ServiceHolder> getImportedServices();

}
