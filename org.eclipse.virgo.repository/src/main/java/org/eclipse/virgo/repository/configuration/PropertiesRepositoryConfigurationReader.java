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

package org.eclipse.virgo.repository.configuration;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.internal.RepositoryLogEvents;
import org.eclipse.virgo.util.common.StringUtils;
import org.eclipse.virgo.util.math.OrderedPair;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public final class PropertiesRepositoryConfigurationReader {

    private static final String REPOSITORY_CHAIN_KEY = "chain";

    private static final String TYPE_SUFFIX = ".type";

    private static final String EXTERNAL_TYPE = "external";

    private static final String SEARCH_PATTERN_SUFFIX = ".searchPattern";

    private static final String WATCHED_TYPE = "watched";

    private static final String WATCH_DIRECTORY_SUFFIX = ".watchDirectory";

    private static final String WATCH_INTERVAL_SUFFIX = ".watchInterval";

    private static final String REMOTE_TYPE = "remote";

    private static final String URI_SUFFIX = ".uri";

    private static final String INDEX_REFRESH_INTERVAL_SUFFIX = ".indexRefreshInterval";

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("(\\$\\{(([^\\}]+))\\})");

    private static final Pattern CONFIG_PATTERN = Pattern.compile("(.*)\\.(.*)");

    private static final int DEFAULT_INDEX_REFRESH_INTERVAL = 30;

    private static final int DEFAULT_WATCH_INTERVAL = 5;

    private final EventLogger eventLogger;

    private final File indexDirectory;

    private final Set<ArtifactBridge> artifactBridges;

    private final String mBeanDomain;
    
    private final File rootDirectory;
    
    public PropertiesRepositoryConfigurationReader(File indexDirectory, Set<ArtifactBridge> artifactBridges, EventLogger eventLogger, String mBeanDomain) {
    	this(indexDirectory, artifactBridges, eventLogger, mBeanDomain, new File("."));
    }

    public PropertiesRepositoryConfigurationReader(File indexDirectory, Set<ArtifactBridge> artifactBridges, EventLogger eventLogger,
        String mBeanDomain, File rootDirectory) {
        this.indexDirectory = indexDirectory;
        this.artifactBridges = artifactBridges;
        this.eventLogger = eventLogger;
        this.mBeanDomain = mBeanDomain;
        this.rootDirectory = rootDirectory;
    }

    /**
     * @param configuration properties
     * @return OrderedPair of a map of configurations and a list of repository names in the chain
     * @throws RepositoryConfigurationException if configuration inconsistent or malformed
     * @see OrderedPair
     */
    public OrderedPair<Map<String, RepositoryConfiguration>, List<String>> readConfiguration(Properties configuration)
        throws RepositoryConfigurationException {
        Map<String, RepositoryConfiguration> repositoryConfigurations = readRepositoriesConfiguration(configuration);

        List<String> chainList = readChainConfiguration(configuration, repositoryConfigurations);

        return new OrderedPair<Map<String, RepositoryConfiguration>, List<String>>(repositoryConfigurations, chainList);
    }

    private List<String> readChainConfiguration(Properties configuration, Map<String, RepositoryConfiguration> repositoryConfigurations) {
        String chainProperty = configuration.getProperty(REPOSITORY_CHAIN_KEY);

        List<String> chainList = new ArrayList<String>();

        if (chainProperty == null) {
            return chainList;
        }

        for (String repositoryName : StringUtils.commaDelimitedListToStringArray(chainProperty)) {
            if (repositoryConfigurations.containsKey(repositoryName)) {
                if (chainList.contains(repositoryName)) {
                    eventLogger.log(RepositoryLogEvents.DUPLICATE_REPOSITORY_IN_CHAIN, repositoryName);
                } else {
                    chainList.add(repositoryName);
                }
            } else {
                eventLogger.log(RepositoryLogEvents.CHAIN_REFERENCES_MISSING_REPOSITORY, repositoryName);
            }
        }
        return chainList;
    }

    private Map<String, RepositoryConfiguration> readRepositoriesConfiguration(Properties configuration) {
        Map<String, RepositoryConfiguration> configurations = new HashMap<String, RepositoryConfiguration>();
        for (String repositoryName : getRepositoryNames(configuration)) {
            RepositoryConfiguration repositoryConfiguration = readRepositoryConfiguration(repositoryName, configuration);
            if (repositoryConfiguration != null) {
                configurations.put(repositoryConfiguration.getName(), repositoryConfiguration);
            }
        }
        return configurations;
    }

    private RepositoryConfiguration readRepositoryConfiguration(String repositoryName, Properties configuration) {
        String type = configuration.getProperty(repositoryName + TYPE_SUFFIX);
        if (type != null) {
            if (EXTERNAL_TYPE.equals(type)) {
                return readExternalRepositoryConfiguration(repositoryName, configuration);
            } else if (REMOTE_TYPE.equals(type)) {
                return readRemoteRepositoryConfiguration(repositoryName, configuration);
            } else if (WATCHED_TYPE.equals(type)) {
                return readWatchedRepositoryConfiguration(repositoryName, configuration);
            } else {
                eventLogger.log(RepositoryLogEvents.UNKNOWN_REPOSITORY_TYPE, type, repositoryName);
            }
        } else {
            eventLogger.log(RepositoryLogEvents.NO_REPOSITORY_TYPE, repositoryName);
        }
        return null;
    }

    private ExternalStorageRepositoryConfiguration readExternalRepositoryConfiguration(String repositoryName, Properties configuration) {
        String searchPattern = configuration.getProperty(repositoryName + SEARCH_PATTERN_SUFFIX);
        if (searchPattern != null) {
            searchPattern = convertPath(searchPattern);
            searchPattern = makeAbsoluteIfNecessary(searchPattern);
            return new ExternalStorageRepositoryConfiguration(repositoryName, new File(this.indexDirectory, repositoryName + ".index"),
                this.artifactBridges, searchPattern, mBeanDomain);
        } else {
            eventLogger.log(RepositoryLogEvents.MISSING_SPECIFICATION, repositoryName, EXTERNAL_TYPE, SEARCH_PATTERN_SUFFIX);
        }
        return null;
    }

    private RemoteRepositoryConfiguration readRemoteRepositoryConfiguration(String repositoryName, Properties configuration) {
        String uri = expandProperties(configuration.getProperty(repositoryName + URI_SUFFIX));
        if (uri != null) {
            String refreshIntervalProperty = repositoryName + INDEX_REFRESH_INTERVAL_SUFFIX;
            int indexRefreshInterval = readIntProperty(refreshIntervalProperty, configuration, DEFAULT_INDEX_REFRESH_INTERVAL);
            
            File cacheDirectory = getCacheDirectory();

            return new RemoteRepositoryConfiguration(repositoryName, new File(this.indexDirectory, repositoryName + ".index"), URI.create(uri),
                indexRefreshInterval, mBeanDomain, cacheDirectory);
        } else {
            eventLogger.log(RepositoryLogEvents.MISSING_SPECIFICATION, repositoryName, REMOTE_TYPE, URI_SUFFIX);
        }
        return null;
    }

    private File getCacheDirectory() {
        return new File(this.indexDirectory, "cache");
    }

    private WatchedStorageRepositoryConfiguration readWatchedRepositoryConfiguration(String repositoryName, Properties configuration) {
        String watchDirPath = expandProperties(configuration.getProperty(repositoryName + WATCH_DIRECTORY_SUFFIX));
        
        if (watchDirPath != null) {
            watchDirPath = makeAbsoluteIfNecessary(watchDirPath);
            String watchIntervalProperty = repositoryName + WATCH_INTERVAL_SUFFIX;
            int watchInterval = readIntProperty(watchIntervalProperty, configuration, DEFAULT_WATCH_INTERVAL);
            return new WatchedStorageRepositoryConfiguration(repositoryName, new File(this.indexDirectory, repositoryName + ".index"), this.artifactBridges, watchDirPath, watchInterval, mBeanDomain);
        } 
        return null;
    }

    private int readIntProperty(String propertyKey, Properties configuration, int defaultValue) {
        String propertyValue = expandProperties(configuration.getProperty(propertyKey));

        if (propertyValue != null) {
            try {
                return Integer.parseInt(propertyValue);
            } catch (NumberFormatException nfe) {
                eventLogger.log(RepositoryLogEvents.MALFORMED_INT_PROPERTY, propertyValue, propertyKey, defaultValue);
            }
        }

        return defaultValue;
    }

    private Set<String> getRepositoryNames(Properties configuration) {
        Set<String> repositoryNames = new HashSet<String>();
        for (Object propertyName : configuration.keySet()) {
            if (!"service.pid".equals(propertyName) && !REPOSITORY_CHAIN_KEY.equals(propertyName)) {
                Matcher matcher = CONFIG_PATTERN.matcher((String) propertyName);
                if (matcher.find()) {
                    String repositoryName = matcher.group(1);
                    if (repositoryName != null)
                        repositoryNames.add(repositoryName);
                }
            }
        }
        return repositoryNames;
    }

    public static String convertToAntStylePath(String searchPath) {
        return searchPath.replaceAll("\\{[^\\}]+\\}", "*");
    }

    private String expandProperties(String value) {
        if (value == null) {
            return value;
        }

        Pattern regex = PROPERTY_PATTERN;
        StringBuffer buffer = new StringBuffer(value.length());
        Matcher matcher = regex.matcher(value);
        int propertyGroup = matcher.groupCount();
        String key, property = "";
        while (matcher.find()) {
            key = matcher.group(propertyGroup);
            property = "";
            if (key.contains("::")) {
                String[] keyDefault = key.split("::");
                property = System.getProperty(keyDefault[0]);
                if (property == null) {
                    property = keyDefault[1];
                } else {
                    property = property.replace('\\', '/');
                }
            } else {
                String systemProperty = System.getProperty(matcher.group(propertyGroup));
                if (systemProperty != null) {
                    property = systemProperty.replace('\\', '/');
                }
            }
            matcher.appendReplacement(buffer, property);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String convertPath(String path) {
        return convertToAntStylePath(expandProperties(path));
    }

    private String makeAbsoluteIfNecessary(String path) {
        String absolutePathPattern;
        if (!path.startsWith("/") && !(path.indexOf(":") > 0)) {
            absolutePathPattern = this.rootDirectory.getAbsolutePath() + File.separator + path;
        } else {
            absolutePathPattern = path;
        }
        if (File.separator.equals("/")) {
            return absolutePathPattern.replace('\\', '/');
        } else {
            return absolutePathPattern.replace('/', '\\');
        }
    }
}
