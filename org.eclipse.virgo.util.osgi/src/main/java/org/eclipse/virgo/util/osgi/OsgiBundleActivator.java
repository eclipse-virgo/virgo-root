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

package org.eclipse.virgo.util.osgi;

import org.eclipse.virgo.util.osgi.internal.StandardPackageAdminUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


/**
 * {@link OsgiBundleActivator} initialises the util.osgi bundle.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is not thread safe.
 * 
 */
public class OsgiBundleActivator implements BundleActivator {

    private StandardPackageAdminUtil standardPackageAdminUtil = null;

    private BundleContext bundleContext;

    private ServiceRegistration packageAdminUtilServiceRegistration;

    /**
     * {@inheritDoc}
     */
    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        if (this.standardPackageAdminUtil == null) {
            this.standardPackageAdminUtil = new StandardPackageAdminUtil(bundleContext);
            this.packageAdminUtilServiceRegistration = this.bundleContext.registerService(PackageAdminUtil.class.getName(),
                this.standardPackageAdminUtil, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext bundleContext) throws Exception {
        if (this.standardPackageAdminUtil != null) {
            this.standardPackageAdminUtil.stop();
            this.packageAdminUtilServiceRegistration.unregister();
            this.standardPackageAdminUtil = null;
        }
    }

}
