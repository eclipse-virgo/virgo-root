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

import static org.eclipse.virgo.test.stubs.internal.Assert.assertNotNull;
import static org.eclipse.virgo.test.stubs.internal.Duplicator.shallowCopy;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * A stub testing implementation of {@link Configuration} as defined in section 104.15.2 of the OSGi Service Platform Service
 * Compendium.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class StubConfiguration implements Configuration {

    private final StubConfigurationAdmin configurationAdmin;

    private final String pid;

    private final String factoryPid;

    private volatile boolean deleted = false;

    private volatile Dictionary<String, Object> properties;

    private final Object propertiesMonitor = new Object();

    private volatile String bundleLocation;

    private final Object bundleLocationMonitor = new Object();

    /**
     * Creates a new {@link StubConfiguration} and sets its initial state. This constructor sets <code>factoryPid</code>
     * to <code>null</code>.
     * 
     * @param pid The pid of this configuration
     */
    public StubConfiguration(String pid) {
        this(pid, null);
    }

    /**
     * Creates a new {@link StubConfiguration} and sets its initial state
     * 
     * @param pid The pid of this configuration
     * @param factoryPid The factory pid of this configuration
     */
    public StubConfiguration(String pid, String factoryPid) {
        this.pid = pid;
        this.factoryPid = factoryPid;
        this.configurationAdmin = new StubConfigurationAdmin(pid == null ? factoryPid : pid, this);
    }

    StubConfiguration(StubConfigurationAdmin configurationAdmin, String pid, String factoryPid, String bundleLocation) {
        this.configurationAdmin = configurationAdmin;
        this.pid = pid;
        this.factoryPid = pid;
        this.bundleLocation = bundleLocation;
    }

    /**
     * {@inheritDoc}
     */
    public void delete() throws IOException {
        this.configurationAdmin.deleteConfiguration(this.pid == null ? this.factoryPid : this.pid);
        this.deleted = true;
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleLocation() {
        synchronized (this.bundleLocationMonitor) {
            return this.bundleLocation;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getFactoryPid() {
        return this.factoryPid;
    }

    /**
     * {@inheritDoc}
     */
    public String getPid() {
        return this.pid;
    }

    /**
     * {@inheritDoc}
     */
    public Dictionary<String, Object> getProperties() {
        synchronized (this.propertiesMonitor) {
            return this.properties == null ? null : shallowCopy(this.properties);
        }
    }

    /**
     * Adds a mapping from a key to a value for all subsequent calls to {@link #getProperties()}.
     * 
     * @param key The key to map from
     * @param value The value to map to
     * @return <code>this</code> instance of the {@link StubConfiguration}
     */
    public StubConfiguration addProperty(String key, Object value) {
        synchronized (this.propertiesMonitor) {
            if (this.properties == null) {
                this.properties = new Hashtable<String, Object>();
                updateSystemProperties(this.properties);
            }

            this.properties.put(key, value);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleLocation(String bundleLocation) {
        synchronized (this.bundleLocationMonitor) {
            this.bundleLocation = bundleLocation;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void update() throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    public void update(Dictionary properties) throws IOException {
        assertNotNull(properties, "properties");
        synchronized (this.propertiesMonitor) {
            Dictionary<String, Object> copy = (Dictionary<String, Object>) shallowCopy(properties);
            updateSystemProperties(copy);
            this.properties = copy;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StubConfiguration other = (StubConfiguration) obj;
        if (!this.pid.equals(other.pid)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.pid.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("pid: %s, factoryPid: %s, deleted: %b", this.pid, this.factoryPid, this.deleted);
    }

    /**
     * Gets whether this {@link Configuration} has been deleted
     * 
     * @return Whether this {@link Configuration} has been deleted
     */
    public boolean getDeleted() {
        return this.deleted;
    }

    private void updateSystemProperties(Dictionary<String, Object> properties) {
        properties.put(Constants.SERVICE_PID, this.pid);
        if (this.factoryPid == null) {
            properties.remove(ConfigurationAdmin.SERVICE_FACTORYPID);
        } else {
            properties.put(ConfigurationAdmin.SERVICE_FACTORYPID, this.factoryPid);
        }
    }
}
