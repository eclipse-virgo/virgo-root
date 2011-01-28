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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.eclipse.virgo.test.framework.dmkernel.DmKernelTestRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

@RunWith(DmKernelTestRunner.class)
public abstract class AbstractKernelIntegrationTest {
    
    protected volatile BundleContext kernelContext;
   
    protected volatile BundleContext context = FrameworkUtil.getBundle(AbstractKernelIntegrationTest.class).getBundleContext();

    protected volatile OsgiFramework framework;

    @Before
    public void setup() {
        ServiceReference<OsgiFramework> serviceReference = this.context.getServiceReference(OsgiFramework.class);
        if (serviceReference != null) {
            this.framework = this.context.getService(serviceReference);
        }
        
        this.kernelContext = getKernelContext();
    }

    private BundleContext getKernelContext() {
        return this.context.getBundle(0L).getBundleContext();
    }

    @BeforeClass
    public static void awaitKernelStartup() throws Exception {
    	MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
    	while (!"STARTED".equals(platformMBeanServer.getAttribute(new ObjectName("org.eclipse.virgo.kernel:type=KernelStatus"), "Status"))) {
    		Thread.sleep(50);
    	}
    }
}
