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

package org.eclipse.virgo.util.osgi.internal;

import org.eclipse.virgo.util.osgi.PackageAdminUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link StandardPackageAdminUtil} is the implementation of {@link PackageAdminUtil}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
public final class StandardPackageAdminUtil implements PackageAdminUtil {

    private static final String PACKAGE_ADMIN_CLASS = "org.osgi.service.packageadmin.PackageAdmin";

    private static final long SLEEP_INTERVAL_MS = 50;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardPackageAdminUtil.class);

    private Object monitor = new Object();

    private boolean started = true;

    private final PackageAdmin packageAdmin;

    private final FrameworkListener frameworkListener;

    private ServiceReference packageAdminServiceReference;

    private final BundleContext bundleContext;

    private volatile boolean refreshComplete = true;

    public StandardPackageAdminUtil(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.packageAdminServiceReference = bundleContext.getServiceReference(PACKAGE_ADMIN_CLASS);
        this.packageAdmin = (PackageAdmin) bundleContext.getService(this.packageAdminServiceReference);
        this.frameworkListener = new FrameworkListener() {

            public void frameworkEvent(FrameworkEvent event) {
                if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
                    synchronized (StandardPackageAdminUtil.this.monitor) {
                        StandardPackageAdminUtil.this.refreshComplete = true;
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Packages refreshed");
                    }
                }
            }
        };
        this.bundleContext.addFrameworkListener(this.frameworkListener);
    }

    public void stop() {
        synchronized (this.monitor) {
            if (started) {
                started = false;
                this.bundleContext.ungetService(this.packageAdminServiceReference);
                this.bundleContext.removeFrameworkListener(this.frameworkListener);
                this.refreshComplete = true;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void refreshPackages(Bundle[] bundles, long timeoutMillis) {
        ensurePreviousRefreshComplete();
        this.packageAdmin.refreshPackages(bundles);
        waitForRefreshToComplete(timeoutMillis);
    }

    // Ensure any previous refresh has completed, and set refreshComplete to false.
    private void ensurePreviousRefreshComplete() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ensuring previous refresh complete");
        }
        while (true) {
            synchronized (this.monitor) {                
                if (this.refreshComplete == true) {
                    this.refreshComplete = false;
                    break;
                }
            }
            sleep(SLEEP_INTERVAL_MS);
        }
    }

    private void waitForRefreshToComplete(long timeoutMillis) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("waiting for refresh to complete");
        }
        long remainingTimeoutMillis = timeoutMillis;
        while (!this.refreshComplete && remainingTimeoutMillis > 0) {
            sleep(SLEEP_INTERVAL_MS);
            remainingTimeoutMillis -= SLEEP_INTERVAL_MS;
        }
        synchronized (this.monitor) {
            this.refreshComplete = true;
        }
    }

    private void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
        }
    }

}
