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

package org.eclipse.virgo.kernel.deployer.core.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.region.Region;

@SuppressWarnings("deprecation")
final class RegionAwarePackageAdminAccessor {
    public static PackageAdmin getPackageAdmin(Region region) {
        BundleContext bundleContext = region.getBundleContext();
        return OsgiFrameworkUtils.getService(bundleContext, PackageAdmin.class).getService();        
    }
}
