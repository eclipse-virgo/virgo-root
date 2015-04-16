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

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

/**
 */
public class KernelConfigurationPropertiesSourceTests {

    @Test
    public void testGetProperties() {
        StubBundleContext context = new StubBundleContext();
        context.addProperty(KernelConfiguration.PROPERTY_KERNEL_HOME, "build/home");
        
        KernelConfiguration configuration = new KernelConfiguration(context);
        
        KernelConfigurationPropertiesSource source = new KernelConfigurationPropertiesSource(configuration);
        
        Map<String, Properties> configurationProperties = source.getConfigurationProperties();
        Properties properties = configurationProperties.get(KernelConfigurationPropertiesSource.KERNEL_CONFIGURATION_PID);
        assertNotNull(properties);
        assertEquals(configuration.getDomain(), properties.getProperty(KernelConfigurationPropertiesSource.PROPERTY_DOMAIN));
        assertEquals(configuration.getHomeDirectory().getAbsolutePath(), properties.getProperty(KernelConfigurationPropertiesSource.PROPERTY_HOME_DIRECTORY));
        assertEquals(configuration.getWorkDirectory().getAbsolutePath(), properties.getProperty(KernelConfigurationPropertiesSource.PROPERTY_WORK_DIRECTORY));
        assertEquals(Integer.toString(configuration.getStartupWaitLimit()), properties.getProperty(KernelConfigurationPropertiesSource.PROPERTY_KERNEL_STARTUP_WAIT_LIMIT));
    }
}
