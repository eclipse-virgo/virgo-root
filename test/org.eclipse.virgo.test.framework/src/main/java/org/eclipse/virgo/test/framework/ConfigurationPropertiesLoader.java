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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.virgo.util.common.PropertyPlaceholderResolver;
import org.eclipse.virgo.util.parser.launcher.ArgumentParser;

public class ConfigurationPropertiesLoader {

    private static final String DEFAULT_USER_CONFIG_LOCATION = "META-INF/test.config.properties";

    private static final String DEFAULT_CONFIG_LOCATION = "org/eclipse/virgo/test/framework/base.configuration.properties";

    private static final String PROP_PROPERTIES_INCLUDE = "org.eclipse.virgo.test.properties.include";

    private static final String PROP_BASEDIR = "basedir";

    Properties loadConfigurationProperties(Class<?> testClass) throws IOException {
        Properties config = new Properties();
        loadProperties(config, getClass().getClassLoader(), DEFAULT_CONFIG_LOCATION, true);

        String userConfigLocation = determineUserConfigLocation(testClass);

        loadProperties(config, testClass.getClassLoader(), userConfigLocation, false);
        loadConfiguredProperties(config);
        addInBuiltProperties(config);

        PropertyPlaceholderResolver resolver = new PropertyPlaceholderResolver();
        return resolver.resolve(config);
    }

    private String determineUserConfigLocation(Class<?> testClass) {
        ConfigLocation configLocation = testClass.getAnnotation(ConfigLocation.class);
        if (configLocation != null) {
            return configLocation.value();
        } else {
            return DEFAULT_USER_CONFIG_LOCATION;
        }
    }

    private void loadConfiguredProperties(Properties config) throws IOException {
        String includeProperties = config.getProperty(PROP_PROPERTIES_INCLUDE);
        if (includeProperties != null && includeProperties.trim().length() > 0) {
            String[] includes = includeProperties.split(",");
            for (String include : includes) {
                URL url = new URL(include.trim());
                try (InputStream s = url.openStream()) {
                    config.load(s);
                }
            }
        }

    }

    private void addInBuiltProperties(Properties config) {
        if (!config.containsKey(PROP_BASEDIR)) {
            config.setProperty(PROP_BASEDIR, System.getProperty("user.dir"));
        }
        config.setProperty("gradle.cache", System.getProperty("user.home") + ArgumentParser.GRADLE_CACHE_RELATIVE);
    }

    private void loadProperties(Properties properties, ClassLoader classLoader, String path, boolean required) throws IOException {
        InputStream stream = classLoader.getResourceAsStream(path);

        if (required && stream == null) {
            throw new IllegalStateException("Unable to locate configuration file '" + path + "' from '" + classLoader + "'");
        }
        if (stream != null) {
            try {
                properties.load(stream);
            } finally {
                stream.close();
            }
        }
    }
}
