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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.virgo.test.stubs.service.cm.StubConfigurationAdmin;
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

        Configuration configuration = configAdmin.getConfiguration(pid, null);
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

        Configuration configuration = configAdmin.getConfiguration(pidOne, null);
        assertConfigurationEquals(configuration, propertiesOne);

        configuration = configAdmin.getConfiguration(pidTwo, null);
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
        propertiesTwo.setProperty("bat", "boo");
        propertiesTwo.setProperty("boo", "bof");

        two.configurationProperties.put(pidTwo, propertiesTwo);

        StubConfigurationAdmin configAdmin = new StubConfigurationAdmin();

        ConfigurationPublisher publisher = new ConfigurationPublisher(configAdmin, one, two);
        publisher.publishConfigurations();

        Configuration configuration = configAdmin.getConfiguration(pidOne, null);

        assertEquals("bar", configuration.getProperties().get("foo"));
        assertEquals("baz", configuration.getProperties().get("bar"));
        assertEquals("boo", configuration.getProperties().get("bat"));
        assertEquals("bof", configuration.getProperties().get("boo"));
    }

    @Test
    public void testPublicationIncludingFactoryConfigurations() throws Exception {
        File[] dirs = new File[] { new File("src/test/resources/UserConfigurationPropertiesSourceTests")};

        UserConfigurationPropertiesSource source = new UserConfigurationPropertiesSource(dirs);

        StubConfigurationAdmin configAdmin = new StubConfigurationAdmin();

        ConfigurationPublisher publisher = new ConfigurationPublisher(configAdmin, source);
        publisher.publishConfigurations();

        // make sure nothing broke on the way here and file name based pids still work
        Configuration three = configAdmin.getConfiguration("three", null);
        assertConfigurationEquals(three, source.getConfigurationProperties().get("three"));

        // check on factories
        List<Configuration> factories = new ArrayList<Configuration>();
        Configuration[] all = configAdmin.listConfigurations(null);
        for (Configuration c : all) {
            if ("one".equals(c.getFactoryPid())) {
                factories.add(c);
            }
        }

        assertEquals(1, factories.size());
    }
    
    @Test
    public void testSingleSourceBadConfiguration() throws IOException {
        StubPropertiesSource source = new StubPropertiesSource();

        String pid = "single";

        Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("FOO", "barbar");

        source.configurationProperties.put(pid, p);

        StubConfigurationAdmin configAdmin = new StubConfigurationAdmin();

        ConfigurationPublisher publisher = new ConfigurationPublisher(configAdmin, source);
        publisher.publishConfigurations();

        Configuration configuration = configAdmin.getConfiguration(pid, null);
        assertEquals(4, configuration.getProperties().size());

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
