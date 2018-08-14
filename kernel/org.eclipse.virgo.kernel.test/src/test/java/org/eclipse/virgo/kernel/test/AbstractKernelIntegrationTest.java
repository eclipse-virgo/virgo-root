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

package org.eclipse.virgo.kernel.test;

import static org.junit.Assert.assertFalse;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.test.framework.dmkernel.DmKernelTestRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

@RunWith(DmKernelTestRunner.class)
public abstract class AbstractKernelIntegrationTest {

    protected volatile BundleContext kernelContext;

    protected final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

    @Before
    public void setup() {
        this.kernelContext = getKernelContext();
    }

    private BundleContext getKernelContext() {
        return this.context.getBundle(0L).getBundleContext();
    }

    @BeforeClass
    public static void awaitKernelStartup() throws Exception {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        int sleepCount = 1000;
        while (!"STARTED".equals(platformMBeanServer.getAttribute(new ObjectName("org.eclipse.virgo.kernel:type=KernelStatus"), "Status"))) {
            Thread.sleep(60);
            if (--sleepCount == 0)
                break;
        }
        assertFalse("Waited for Kernel too long.", sleepCount == 0);
    }
}
