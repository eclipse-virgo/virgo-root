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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import org.eclipse.osgi.framework.internal.core.PackageAdminImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;

/**
 * {@link StandardPackageAdminUtil} is the implementation of {@link PackageAdminUtil}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
@SuppressWarnings("deprecation")
public final class StandardPackageAdminUtil implements PackageAdminUtil {

    private final PackageAdmin packageAdmin;

    public StandardPackageAdminUtil(BundleContext bundleContext) {
        this.packageAdmin = OsgiFrameworkUtils.getService(bundleContext, PackageAdmin.class).getService();
    }
    
    /** 
     * {@inheritDoc}
     */
    public void synchronouslyRefreshPackages(Bundle[] bundles) {
        ((PackageAdminImpl)this.packageAdmin).refreshPackages(bundles, true, null);        
    }
}
