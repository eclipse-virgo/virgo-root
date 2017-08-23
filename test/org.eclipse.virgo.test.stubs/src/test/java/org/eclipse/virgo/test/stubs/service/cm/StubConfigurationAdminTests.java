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

package org.eclipse.virgo.test.stubs.service.cm;

import static org.eclipse.virgo.test.stubs.AdditionalAsserts.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;

public class StubConfigurationAdminTests {

    private final StubConfigurationAdmin configAdmin = new StubConfigurationAdmin();

    @Test(expected = IllegalArgumentException.class)
    public void createFactoryConfigNullInput() throws IOException {
        configAdmin.createFactoryConfiguration(null);
    }

    @Test
    public void createFactoryConfig() throws IOException {
        Configuration config = configAdmin.createFactoryConfiguration("test");
        assertNull(config.getBundleLocation());
        assertNull(config.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFactoryConfigWithLocationNullInput() throws IOException {
        configAdmin.createFactoryConfiguration(null, null);
    }

    @Test
    public void createFactoryConfigWithLocation() throws IOException {
        Configuration config = configAdmin.createFactoryConfiguration("test", null);
        assertNull(config.getBundleLocation());
        assertNull(config.getProperties());

        Configuration config1 = configAdmin.createFactoryConfiguration("test", "test");
        assertEquals("test", config1.getBundleLocation());
        assertNull(config1.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigNullInput() throws IOException {
        configAdmin.getConfiguration(null);
    }

    @Test
    public void getConfig() throws IOException {
        Configuration config = configAdmin.getConfiguration("test");
        assertNull(config.getBundleLocation());
        assertNull(config.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigWithLocationNullInput() throws IOException {
        configAdmin.getConfiguration(null, null);
    }

    @Test
    public void getConfigWithLocationExists() throws IOException {
        Configuration config = configAdmin.getConfiguration("test", null);
        config.update(new Hashtable<String, Object>());

        Configuration config1 = configAdmin.getConfiguration("test", null);
        assertNotNull(config1.getProperties());
    }

    @Test
    public void getConfigWithLocationDoesNotExist() throws IOException {
        Configuration config = configAdmin.getConfiguration("test", null);
        assertNull(config.getBundleLocation());
        assertNull(config.getProperties());

        Configuration config1 = configAdmin.getConfiguration("test1", "test");
        assertEquals("test", config1.getBundleLocation());
        assertNull(config1.getProperties());
    }

    @Test
    public void listConfigurationsNullFilter() throws IOException, InvalidSyntaxException {
        configAdmin.createConfiguration("test1").addProperty("key1", "value1");
        configAdmin.createConfiguration("test2");
        assertEquals(1, configAdmin.listConfigurations(null).length);
    }

    @Test
    public void listConfigurationsNoMatches() throws IOException, InvalidSyntaxException {
        assertNull(configAdmin.listConfigurations(null));
    }

    @Test
    public void listConfigurations() throws IOException, InvalidSyntaxException {
        configAdmin.createConfiguration("test1").addProperty("key", "value");
        configAdmin.createConfiguration("test2");
        assertEquals(1, configAdmin.listConfigurations("(key=value)").length);
    }

    @Test
    public void testToString() {
        StubConfigurationAdmin configAdmin1 = new StubConfigurationAdmin("test", new StubConfiguration("test"));
        String toString = configAdmin1.toString();
        assertContains("configurations", toString);
        assertContains("pid", toString);
        assertContains("factoryPid", toString);
        assertContains("deleted", toString);

    }
}
