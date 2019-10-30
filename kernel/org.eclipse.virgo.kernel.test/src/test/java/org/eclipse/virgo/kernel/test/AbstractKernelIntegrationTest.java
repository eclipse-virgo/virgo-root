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

import org.eclipse.virgo.test.framework.dmkernel.DmKernelTestRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertNotEquals;

@RunWith(DmKernelTestRunner.class)
public abstract class AbstractKernelIntegrationTest {

    protected volatile BundleContext kernelContext;

    protected final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

    @Before
    public void setup() {
        this.kernelContext = getKernelContext();
    }

    @BeforeClass
    public static void awaitKernelStartup() throws Exception {
        if (System.getProperty("await.kernel.startup", "true").equals("true")) {
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            int sleepCount = 1000;
            while (!"STARTED".equals(platformMBeanServer.getAttribute(new ObjectName("org.eclipse.virgo.kernel:type=KernelStatus"), "Status"))) {
                Thread.sleep(60);
                if (--sleepCount == 0)
                    break;
            }
            assertNotEquals("Waited for Kernel too long.", 0, sleepCount);
        }
    }

    private BundleContext getKernelContext() {
        return this.context.getBundle(0L).getBundleContext();
    }

}
