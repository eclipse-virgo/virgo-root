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

package org.eclipse.virgo.nano.config.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import org.eclipse.virgo.test.stubs.framework.StubBundleContext;


/**
 */
public class KernelConfigurationTests {

    @Test
    public void testCreateMinimalKernelConfiguration() {
        StubBundleContext context = new StubBundleContext();
        context.addProperty(KernelConfiguration.PROPERTY_KERNEL_HOME, "build");
        
        KernelConfiguration configuration = new KernelConfiguration(context);
        assertEquals(new File("build"), configuration.getHomeDirectory());
        assertNotNull(configuration.getWorkDirectory());
        assertNotNull(configuration.getConfigDirectories());
        assertEquals(1, configuration.getConfigDirectories().length);
        assertNotNull(configuration.getDomain());
        assertNotNull(configuration.getStartupWaitLimit());
    }
    
    @Test
    public void testCustomConfiguration() {
        StubBundleContext context = new StubBundleContext();
        context.addProperty(KernelConfiguration.PROPERTY_KERNEL_HOME, "build");
        context.addProperty(KernelConfiguration.PROPERTY_KERNEL_DOMAIN, "my.domain");
        context.addProperty(KernelConfiguration.PROPERTY_KERNEL_CONFIG, "foo,bar");
        context.addProperty(KernelConfiguration.PROPERTY_KERNEL_STARTUP_WAIT_LIMIT, "60");
        
        KernelConfiguration configuration = new KernelConfiguration(context);
        assertEquals(new File("build"), configuration.getHomeDirectory());
        assertNotNull(configuration.getWorkDirectory());
        assertNotNull(configuration.getConfigDirectories());
        assertEquals(2, configuration.getConfigDirectories().length);
        assertEquals("my.domain", configuration.getDomain());
        assertEquals(60, configuration.getStartupWaitLimit());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testMissingKernelHomeProperty() {
        new KernelConfiguration(new StubBundleContext());
    }
}
