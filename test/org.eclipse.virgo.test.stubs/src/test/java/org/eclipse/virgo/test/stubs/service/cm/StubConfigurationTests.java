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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class StubConfigurationTests {

    private final StubConfiguration config = new StubConfiguration("test");

    @Test
    public void delete() throws IOException {
        config.delete();
        assertTrue(config.getDeleted());
        StubConfiguration config1 = new StubConfiguration(null, "test");
        config1.delete();
        assertTrue(config1.getDeleted());
    }

    @Test(expected = IllegalStateException.class)
    public void deleteAfterDelete() throws IOException {
        config.delete();
        config.delete();
    }

    @Test
    public void bundleLocation() {
        assertNull(this.config.getBundleLocation());
        this.config.setBundleLocation("test");
        assertEquals("test", this.config.getBundleLocation());
        this.config.setBundleLocation(null);
        assertNull(this.config.getBundleLocation());
    }

    @Test(expected = IllegalStateException.class)
    public void getBundleLocationAfterDelete() throws IOException {
        this.config.delete();
        this.config.getBundleLocation();
    }

    @Test(expected = IllegalStateException.class)
    public void setBundleLocationAfterDelete() throws IOException {
        this.config.delete();
        this.config.setBundleLocation("test");
    }

    @Test
    public void factoryPidNull() {
        assertNull(this.config.getFactoryPid());
    }

    @Test
    public void factoryPid() {
        Configuration config1 = new StubConfiguration(null, "test");
        assertEquals("test", config1.getFactoryPid());
    }

    @Test(expected = IllegalStateException.class)
    public void getFactoryPidAfterDelete() throws IOException {
        this.config.delete();
        this.config.getFactoryPid();
    }

    @Test
    public void pid() {
        assertEquals("test", this.config.getPid());
    }

    @Test
    public void pidNull() {
        Configuration config1 = new StubConfiguration(null, "test");
        assertNull(config1.getPid());
    }

    @Test(expected = IllegalStateException.class)
    public void getPidAfterDelete() throws IOException {
        this.config.delete();
        this.config.getPid();
    }

    @Test
    public void addProperty() throws IOException {
        this.config.update(new Hashtable<String, String>());
        this.config.addProperty("test1", "test2");
        assertEquals(2, this.config.getProperties().size());
        assertEquals("test2", this.config.getProperties().get("test1"));
    }

    @Test
    public void addPropertyNull() {
        this.config.addProperty("test1", "test2");
        assertEquals(2, this.config.getProperties().size());
        assertEquals("test2", this.config.getProperties().get("test1"));
    }

    @Test
    public void getProperties() throws IOException {
        this.config.update(new Hashtable<String, String>());

        Dictionary<String, Object> properties1 = this.config.getProperties();
        Dictionary<String, Object> properties2 = this.config.getProperties();
        assertNotSame(properties1, properties2);
        properties2.put("test3", "test4");
        assertFalse(properties2.equals(this.config.getProperties()));
        assertNull(this.config.getProperties().get(ConfigurationAdmin.SERVICE_BUNDLELOCATION));
    }

    @Test
    public void getPropertiesNull() {
        assertNull(this.config.getProperties());
    }

    @Test(expected = IllegalStateException.class)
    public void getPropertiesAfterDeleted() throws IOException {
        this.config.delete();
        this.config.getProperties();
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNull() throws IOException {
        this.config.update(null);
    }

    @Test
    public void updateAddProperties() throws IOException {
        assertNull(this.config.getProperties());
        this.config.update(new Hashtable<String, Object>());
        assertEquals(1, this.config.getProperties().size());
        assertEquals("test", this.config.getProperties().get(Constants.SERVICE_PID));

        Configuration config1 = new StubConfiguration("test1", "test2");
        config1.update(new Hashtable<String, Object>());
        assertEquals(2, config1.getProperties().size());
        assertEquals("test1", config1.getProperties().get(Constants.SERVICE_PID));
        assertEquals("test2", config1.getProperties().get(ConfigurationAdmin.SERVICE_FACTORYPID));
    }

    @Test
    public void updateOverwriteProperties() throws IOException {
        assertNull(this.config.getProperties());

        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(Constants.SERVICE_PID, "test2");
        properties.put(ConfigurationAdmin.SERVICE_FACTORYPID, "test3");

        this.config.update(properties);
        assertEquals(1, this.config.getProperties().size());
        assertEquals("test", this.config.getProperties().get(Constants.SERVICE_PID));

        Configuration config1 = new StubConfiguration("test1", "test2");
        config1.update(properties);
        assertEquals(2, config1.getProperties().size());
        assertEquals("test1", config1.getProperties().get(Constants.SERVICE_PID));
        assertEquals("test2", config1.getProperties().get(ConfigurationAdmin.SERVICE_FACTORYPID));
    }

    @Test(expected = IllegalStateException.class)
    public void updatePropertiesAfterDelete() throws IOException {
        this.config.delete();
        this.config.update(new Hashtable<String, Object>());
    }

    @Test
    public void update() throws IOException {
        this.config.update();
    }

    @Test(expected = IllegalStateException.class)
    public void updateAfterDelete() throws IOException {
        this.config.delete();
        this.config.update();
    }

    @Test
    public void testEquals() {
        assertTrue(this.config.equals(this.config));
        assertFalse(this.config.equals(null));
        assertFalse(this.config.equals(new Object()));
        assertTrue(this.config.equals(new StubConfiguration("test")));
        assertFalse(this.config.equals(new StubConfiguration("test2")));
    }

    @Test
    public void testHashCode() {
        assertEquals("test".hashCode(), this.config.hashCode());
    }

    @Test
    public void testToString() {
        String toString = config.toString();
        assertContains("pid", toString);
        assertContains("factoryPid", toString);
        assertContains("deleted", toString);
    }
}
