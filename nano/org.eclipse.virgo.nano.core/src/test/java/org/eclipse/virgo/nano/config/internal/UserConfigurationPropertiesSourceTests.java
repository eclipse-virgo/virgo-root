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

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.eclipse.virgo.nano.config.internal.UserConfigurationPropertiesSource;
import org.junit.Test;


import static org.junit.Assert.*;


/**
 */
public class UserConfigurationPropertiesSourceTests {

    @Test
    public void testReadUserConfiguration() {
        File[] dirs = new File[]{
            new File("src/test/resources/" + getClass().getSimpleName())
        };
        
        UserConfigurationPropertiesSource source = new UserConfigurationPropertiesSource(dirs);
        Map<String, Properties> properties = source.getConfigurationProperties();
        
        Properties one = properties.get("one");
        Properties two = properties.get("two");
        Properties three = properties.get("three");
        assertNotNull(one);
        assertNotNull(two);
        assertNotNull(three);
        assertEquals("bar", one.getProperty("baz"));
        assertEquals("baz", two.getProperty("foo"));
        assertEquals("three", three.getProperty("foo"));
    }
    
    @Test
    public void testReadFactoryUserConfiguration() {
        File[] dirs = new File[]{
            new File("src/test/resources/" + getClass().getSimpleName())
        };
        
        UserConfigurationPropertiesSource source = new UserConfigurationPropertiesSource(dirs);
        Map<String, Properties> properties = source.getConfigurationProperties();
        
        assertEquals(5, properties.size());
        
        Properties one = properties.get("factory.pid-factoryOne");
        Properties two = properties.get("factory.pid-factoryTwo");
        
        assertNotNull(one);
        assertNotNull(two);
        assertEquals("one", one.getProperty("foo"));
        assertEquals("two", two.getProperty("foo"));
    }
}
