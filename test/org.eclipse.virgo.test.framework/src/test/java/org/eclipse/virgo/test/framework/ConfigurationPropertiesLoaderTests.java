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

package org.eclipse.virgo.test.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.virgo.test.framework.ConfigLocation;
import org.eclipse.virgo.test.framework.ConfigurationPropertiesLoader;
import org.junit.Test;

public class ConfigurationPropertiesLoaderTests {

    @Test
    public void testLoadProperties() throws IOException {
        ConfigurationPropertiesLoader loader = new ConfigurationPropertiesLoader();
        Properties properties = loader.loadConfigurationProperties(getClass());
        
        assertNotNull(properties);
        
        assertEquals("true", properties.getProperty("user"));
        assertEquals("true", properties.getProperty("extra"));
    }
    
    @Test
    public void testInBuiltProperties() throws IOException {
        ConfigurationPropertiesLoader loader = new ConfigurationPropertiesLoader();
        Properties properties = loader.loadConfigurationProperties(getClass());
        
        assertNotNull(properties);
        
        assertNotNull(properties.getProperty("basedir"));
    }
    
    @Test
    public void testResolve() throws IOException {
        ConfigurationPropertiesLoader loader = new ConfigurationPropertiesLoader();
        Properties properties = loader.loadConfigurationProperties(getClass());
        
        assertNotNull(properties);
        
        assertEquals("true", properties.getProperty("replace"));
    }
    
    @Test
    public void customConfigLocation() throws IOException {
        ConfigurationPropertiesLoader loader = new ConfigurationPropertiesLoader();
        Properties properties = loader.loadConfigurationProperties(CustomConfigLocation.class);
        assertEquals("true", properties.getProperty("custom"));
    }
    
    @ConfigLocation("META-INF/custom.config.properties")
    private static final class CustomConfigLocation {
        
    }
}
