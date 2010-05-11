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

package org.eclipse.virgo.kernel.config.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.virgo.kernel.StubConfigurationAdmin;
import org.eclipse.virgo.kernel.config.internal.ConfigurationPublisher;
import org.eclipse.virgo.kernel.config.internal.PropertiesSource;
import org.junit.Test;
import org.osgi.service.cm.Configuration;


/**
 */
public class ConfigurationPublisherTests {

    @Test
    public void testSingleSource() throws IOException {
        StubPropertiesSource source = new StubPropertiesSource();

        String pid = "single";

        Properties p = new Properties();
        p.setProperty("foo", "bar");

        source.configurationProperties.put(pid, p);

        StubConfigurationAdmin configAdmin = new StubConfigurationAdmin();

        ConfigurationPublisher publisher = new ConfigurationPublisher(configAdmin, source);
        publisher.publishConfigurations();

        Configuration configuration = configAdmin.getConfiguration(pid);
        assertConfigurationEquals(configuration, p);

    }

    @Test
    public void testTwoSources() throws Exception {
        StubPropertiesSource one = new StubPropertiesSource();
        StubPropertiesSource two = new StubPropertiesSource();

        // setup one
        String pidOne = "one";

        Properties propertiesOne = new Properties();
        propertiesOne.setProperty("foo", "bar");

        one.configurationProperties.put(pidOne, propertiesOne);

        // setup two
        String pidTwo = "two";

        Properties propertiesTwo = new Properties();
        propertiesTwo.setProperty("bar", "baz");

        two.configurationProperties.put(pidTwo, propertiesTwo);

        StubConfigurationAdmin configAdmin = new StubConfigurationAdmin();

        ConfigurationPublisher publisher = new ConfigurationPublisher(configAdmin, one, two);
        publisher.publishConfigurations();

        Configuration configuration = configAdmin.getConfiguration(pidOne);
        assertConfigurationEquals(configuration, propertiesOne);
        
        configuration = configAdmin.getConfiguration(pidTwo);
        assertConfigurationEquals(configuration, propertiesTwo);

    }

    @Test
    public void testMultiSourceMerge() throws Exception {
        StubPropertiesSource one = new StubPropertiesSource();
        StubPropertiesSource two = new StubPropertiesSource();

        // setup one
        String pidOne = "one";

        Properties propertiesOne = new Properties();
        propertiesOne.setProperty("foo", "bar");
        propertiesOne.setProperty("bar", "baz");

        one.configurationProperties.put(pidOne, propertiesOne);

        // setup two
        String pidTwo = pidOne;

        Properties propertiesTwo = new Properties();
        propertiesTwo.setProperty("bar", "boo");
        propertiesTwo.setProperty("boo", "bof");

        two.configurationProperties.put(pidTwo, propertiesTwo);

        StubConfigurationAdmin configAdmin = new StubConfigurationAdmin();

        ConfigurationPublisher publisher = new ConfigurationPublisher(configAdmin, one, two);
        publisher.publishConfigurations();

        Configuration configuration = configAdmin.getConfiguration(pidOne);

        assertEquals("bar", configuration.getProperties().get("foo"));
        assertEquals("boo", configuration.getProperties().get("bar"));
        assertEquals("bof", configuration.getProperties().get("boo"));
    }

    private void assertConfigurationEquals(Configuration configuration, Properties properties) {
        for (String s : properties.stringPropertyNames()) {
            assertEquals(properties.getProperty(s), configuration.getProperties().get(s));
        }
    }

    private static class StubPropertiesSource implements PropertiesSource {

        private final Map<String, Properties> configurationProperties = new TreeMap<String, Properties>();

        /**
         * {@inheritDoc}
         */
        public Map<String, Properties> getConfigurationProperties() {
            return this.configurationProperties;
        }

    }
}
