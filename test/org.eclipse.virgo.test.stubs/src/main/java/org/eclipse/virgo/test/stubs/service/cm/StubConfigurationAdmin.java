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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import org.eclipse.virgo.test.stubs.support.PropertiesFilter;
import org.eclipse.virgo.test.stubs.support.TrueFilter;

/**
 * A stub testing implementation of {@link ConfigurationAdmin} as defined in section 104.15.3 of the OSGi Service Platform
 * Service Compendium.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class StubConfigurationAdmin implements ConfigurationAdmin {

    private final Map<String, Configuration> configurations = new HashMap<String, Configuration>();

    private final Object configurationsMonitor = new Object();

    /**
     * Creates a new {@link StubConfigurationAdmin} and sets its initial state
     */
    public StubConfigurationAdmin() {
    }

    StubConfigurationAdmin(String pid, Configuration configuration) {
        this.configurations.put(pid, configuration);
    }

    /**
     * {@inheritDoc}
     */
    public Configuration createFactoryConfiguration(String factoryPid) throws IOException {
        return createFactoryConfiguration(factoryPid, null);
    }

    /**
     * {@inheritDoc}
     */
    public Configuration createFactoryConfiguration(String factoryPid, String location) throws IOException {
        assertNotNull(factoryPid, "factoryPid");
        synchronized (this.configurationsMonitor) {
            this.configurations.put(factoryPid, new StubConfiguration(this, null, factoryPid, location));
            return this.configurations.get(factoryPid);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Configuration getConfiguration(String pid) throws IOException {
        return getConfiguration(pid, null);
    }

    /**
     * Create a new configuration in this {@link ConfigurationAdmin}. This method is useful for chaining together the
     * creation and property population of stub {@link Configuration}s
     * 
     * @param pid The pid of the {@link Configuration} being created
     * @return the {@link StubConfiguration} that was created
     * @throws IOException required by the {@link ConfigurationAdmin} specification
     */
    public StubConfiguration createConfiguration(String pid) throws IOException {
        return (StubConfiguration) getConfiguration(pid);
    }

    /**
     * {@inheritDoc}
     */
    public Configuration getConfiguration(String pid, String location) throws IOException {
        assertNotNull(pid, "pid");
        synchronized (this.configurationsMonitor) {
            if (!this.configurations.containsKey(pid)) {
                this.configurations.put(pid, new StubConfiguration(this, pid, null, location));
            }
            return this.configurations.get(pid);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Configuration[] listConfigurations(String filter) throws IOException, InvalidSyntaxException {
        synchronized (this.configurationsMonitor) {
            Set<Configuration> matches = new HashSet<Configuration>();

            Filter f = filter == null ? new TrueFilter() : new PropertiesFilter(filter);
            for (Configuration configuration : this.configurations.values()) {
                if (isCurrent(configuration) && f.match(configuration.getProperties())) {
                    matches.add(configuration);
                }
            }

            return matches.size() == 0 ? null : matches.toArray(new Configuration[matches.size()]);
        }
    }

    private boolean isCurrent(Configuration configuration) {
        return configuration.getProperties() != null;
    }

    void deleteConfiguration(String pid) {
        synchronized (this.configurationsMonitor) {
            this.configurations.remove(pid);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("configurations: %s", this.configurations);
    }

}
