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

package org.eclipse.virgo.util.test;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class PackageAdminUtilTests extends AbstractEquinoxLaunchingTests {

    private static final String PACKAGE_ADMIN_UTIL_CLASS_NAME = "org.eclipse.virgo.util.osgi.PackageAdminUtil";     

    protected Exception fail;

    @Before
    public void setup() throws Exception {
        
        this.bundleContext.installBundle(new File("../org.eclipse.virgo.util.common/target/classes").toURI().toString()).start();
        this.bundleContext.installBundle(new File("../org.eclipse.virgo.util.parser.manifest/target/classes").toURI().toString()).start();
        this.bundleContext.installBundle(new File("../org.eclipse.virgo.util.osgi/target/classes").toURI().toString()).start();
        this.fail = null;
    }
    
    @Override
    protected String getSystemPackages() {
        return "org.slf4j;version=1.5.10," + "org.eclipse.virgo.util.common;version=2.0.0," + "org.eclipse.virgo.util.math;version=2.0.0";
    }

    @Test
    public void testSynchronousRefreshPackages() throws IllegalArgumentException, SecurityException {
        ServiceReference packageAdminUtilServiceReference = this.bundleContext.getServiceReference(PACKAGE_ADMIN_UTIL_CLASS_NAME);
        Object packageAdminUtil = this.bundleContext.getService(packageAdminUtilServiceReference);
        driveRefreshPackages(packageAdminUtil, 50);
    }

    @Test
    public void testOverlappingSynchronousRefreshPackages() throws IllegalArgumentException, SecurityException {
        ServiceReference packageAdminUtilServiceReference = this.bundleContext.getServiceReference(PACKAGE_ADMIN_UTIL_CLASS_NAME);
        final Object packageAdminUtil = this.bundleContext.getService(packageAdminUtilServiceReference);
        for (int i = 0; i < 10; i++) {
            final int j = i;
            new Thread() {

                @Override
                public void run() {
                    PackageAdminUtilTests.this.driveRefreshPackages(packageAdminUtil, 50 - j * 5);
                }
            };
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.fail = e;
        }
        Assert.assertNull(this.fail);
    }

    private void driveRefreshPackages(Object packageAdminUtil, int timeout) {
        try {
            ((Class<?>) packageAdminUtil.getClass()).getDeclaredMethod("refreshPackages", new Class<?>[] { new Bundle[0].getClass(), long.class }).invoke(
                packageAdminUtil, new Object[] { null, new Long(timeout) });
        } catch (Exception e) {
            e.printStackTrace();
            this.fail = e;
        }
    }
}
