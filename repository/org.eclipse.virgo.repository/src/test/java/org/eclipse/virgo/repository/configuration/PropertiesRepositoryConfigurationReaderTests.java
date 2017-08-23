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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.PropertiesRepositoryConfigurationReader;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RepositoryConfigurationException;
import org.eclipse.virgo.repository.configuration.WatchedStorageRepositoryConfiguration;
import org.eclipse.virgo.util.math.OrderedPair;

public class PropertiesRepositoryConfigurationReaderTests {
    
    private PropertiesRepositoryConfigurationReader configurationReader;
    
    private MockEventLogger eventLogger = new MockEventLogger();
    
    @Before
    public void createConfigurationReader() {
        eventLogger.reinitialise();
        configurationReader = new PropertiesRepositoryConfigurationReader(new File("indices"), Collections.<ArtifactBridge>emptySet(), eventLogger, null);
    }

    @Test
    public void allRepositoryTypes() throws RepositoryConfigurationException {                
        Properties properties = new Properties();
        
        properties.setProperty("a.type", "external");
        properties.setProperty("a.searchPattern", "/**/*.jar");
        
        properties.setProperty("b.type", "watched");
        properties.setProperty("b.watchInterval", "5");
        properties.setProperty("b.watchDirectory", "build/watched");
        
        properties.setProperty("c.type", "remote");
        properties.setProperty("c.indexRefreshInterval", "5");
        properties.setProperty("c.uri", "http://watched.com");
        
        properties.setProperty("chain", "a,b,c");
        
        Map<String,RepositoryConfiguration> configurationMap = configurationReader.readConfiguration(properties).getFirst();
        
        assertEquals(0, this.eventLogger.getLoggedEvents().size());

        assertEquals(3, configurationMap.size());

        assertTrue("Cannot find confguration 'a'", configurationMap.containsKey("a"));
        assertEquals(ExternalStorageRepositoryConfiguration.class, configurationMap.get("a").getClass());
        
        assertTrue("Cannot find confguration 'b'", configurationMap.containsKey("b"));
        assertEquals(WatchedStorageRepositoryConfiguration.class, configurationMap.get("b").getClass());
        
        assertTrue("Cannot find confguration 'c'", configurationMap.containsKey("c"));
        assertEquals(RemoteRepositoryConfiguration.class, configurationMap.get("c").getClass());
    }
    
    @Test
    public void absoluteWatchDirectory() throws RepositoryConfigurationException, IOException {
        configurationReader = new PropertiesRepositoryConfigurationReader(new File("indices"), Collections.<ArtifactBridge>emptySet(), eventLogger, null, new File("foo/bar").getAbsoluteFile());
        
        Properties properties = new Properties();
        
        properties.setProperty("b.type", "watched");
        properties.setProperty("b.watchInterval", "5");
        properties.setProperty("b.watchDirectory", "build/watched");
        
        Map<String, RepositoryConfiguration> configuration = configurationReader.readConfiguration(properties).getFirst();
        RepositoryConfiguration repositoryConfiguration = configuration.get("b");
        
        assertNotNull(repositoryConfiguration);
        assertTrue(repositoryConfiguration instanceof WatchedStorageRepositoryConfiguration);
        
        File directoryToWatch = ((WatchedStorageRepositoryConfiguration)repositoryConfiguration).getDirectoryToWatch();
        assertEquals(new File("foo/bar/build/watched").getCanonicalFile(), directoryToWatch.getCanonicalFile());
    }
    
    @Test
    public void unknownRepositoryTypes() throws RepositoryConfigurationException {                
        Properties properties = new Properties();
        
        properties.setProperty("a.type", "whatever");
        properties.setProperty("a.searchPattern", "/**/*.jar");
        
        properties.setProperty("b.watchInterval", "5");
        properties.setProperty("b.watchDirectory", "build/watched");
        
        Map<String,RepositoryConfiguration> configurationMap = configurationReader.readConfiguration(properties).getFirst();
        
        assertEquals(2, this.eventLogger.getLoggedEvents().size());
        assertEquals(0, configurationMap.size());
    }

    @Test
    public void chainReferencingNonExistentRepository() throws RepositoryConfigurationException {
        Properties properties = new Properties();                        
        
        properties.setProperty("a.type", "external");
        properties.setProperty("a.searchPattern", "/**/*.jar");
        
        properties.setProperty("chain", "a,b");
        
        OrderedPair<Map<String,RepositoryConfiguration>,List<String>> configurations = configurationReader.readConfiguration(properties);
        Map<String,RepositoryConfiguration> configurationMap = configurations.getFirst();
        List<String> chainList = configurations.getSecond();
        
        assertEquals(1, this.eventLogger.getLoggedEvents().size());
        assertEquals(1, configurationMap.size());
        assertEquals(1, chainList.size());
    }

    @Test
    public void emptyChainProperty() throws RepositoryConfigurationException {
        Properties properties = new Properties();                        
        
        properties.setProperty("a.type", "external");
        properties.setProperty("a.searchPattern", "/**/*.jar");
        
        List<String> chainList = configurationReader.readConfiguration(properties).getSecond();
        assertEquals(0, this.eventLogger.getLoggedEvents().size());
        assertEquals(0, chainList.size());
    }

    @Test
    public void duplicateRepositoriesInChain() throws RepositoryConfigurationException {
        Properties properties = new Properties();                        
        
        properties.setProperty("a.type", "external");
        properties.setProperty("a.searchPattern", "/**/*.jar");
        
        properties.setProperty("chain", "a,a");
        
        List<String> chainList = configurationReader.readConfiguration(properties).getSecond();
        assertEquals(1, this.eventLogger.getLoggedEvents().size());
        assertEquals(1, chainList.size());
    }
    
    @Test
    public void defaultingOfIndexRefreshIntervalAndWatchInterval() throws RepositoryConfigurationException {
        Properties properties = new Properties();
        properties.setProperty("watched-repo.type", "watched");
        properties.setProperty("watched-repo.watchDirectory", "build/repository/watched");
        properties.setProperty("remote-repo.type", "remote");
        properties.setProperty("remote-repo.uri", "http://localhost:8080/org.eclipse.virgo.repository/foo");
        properties.setProperty("chain", "watched-repo,remote-repo");

        OrderedPair<Map<String,RepositoryConfiguration>,List<String>> configurations = configurationReader.readConfiguration(properties);
        Map<String,RepositoryConfiguration> configurationMap = configurations.getFirst();
        List<String> chainList = configurations.getSecond();

        assertEquals(2, chainList.size());
        assertEquals(2, configurationMap.size());
        
        RepositoryConfiguration configuration = configurationMap.get(chainList.get(0));
        assertTrue(configuration instanceof WatchedStorageRepositoryConfiguration);
        assertEquals(5, ((WatchedStorageRepositoryConfiguration)configuration).getWatchInterval());
        
        configuration = configurationMap.get(chainList.get(1));
        assertTrue(configuration instanceof RemoteRepositoryConfiguration);
        assertEquals(30, ((RemoteRepositoryConfiguration)configuration).getIndexUpdateInterval());
        
        assertEquals(0, eventLogger.getLoggedEvents().size());
    }
    
    @Test
    public void defaultingOfIndexRefreshIntervalAndWatchIntervalWhenMalformed() throws RepositoryConfigurationException {
        Properties properties = new Properties();
        properties.setProperty("watched-repo.type", "watched");
        properties.setProperty("watched-repo.watchDirectory", "build/repository/watched");
        properties.setProperty("watched-repo.watchInterval", "alpha");
        properties.setProperty("remote-repo.type", "remote");
        properties.setProperty("remote-repo.uri", "http://localhost:8080/org.eclipse.virgo.repository/foo");
        properties.setProperty("remote-repo.indexRefreshInterval", "bravo");
        properties.setProperty("chain", "watched-repo,remote-repo");
        
        OrderedPair<Map<String,RepositoryConfiguration>,List<String>> configurations = configurationReader.readConfiguration(properties);
        Map<String,RepositoryConfiguration> configurationMap = configurations.getFirst();
        List<String> chainList = configurations.getSecond();
        
        assertEquals(2, chainList.size());
        assertEquals(2, configurationMap.size());

        RepositoryConfiguration configuration = configurationMap.get(chainList.get(0));
        assertTrue(configuration instanceof WatchedStorageRepositoryConfiguration);
        assertEquals(5, ((WatchedStorageRepositoryConfiguration)configuration).getWatchInterval());
        
        configuration = configurationMap.get(chainList.get(1));
        assertTrue(configuration instanceof RemoteRepositoryConfiguration);
        assertEquals(30, ((RemoteRepositoryConfiguration)configuration).getIndexUpdateInterval());
        
        assertEquals(2, eventLogger.getLoggedEvents().size());
    }
    
    @Test
    public void variableExpansion() throws RepositoryConfigurationException, IOException {
        try {
            System.setProperty("org.eclipse.virgo.repository.internal.test.string", "a");
            System.setProperty("org.eclipse.virgo.repository.internal.test.int", "1");
            
            Properties properties = new Properties();
            
            properties.setProperty("bundles-ext.type", "external");
            properties.setProperty("bundles-ext.searchPattern", "repository/${org.eclipse.virgo.repository.internal.test.string}/ext/*.jar");

            properties.setProperty("watched-repo.type", "watched");
            properties.setProperty("watched-repo.watchDirectory", "build/repository/${org.eclipse.virgo.repository.internal.test.string}");
            properties.setProperty("watched-repo.watchInterval", "${org.eclipse.virgo.repository.internal.test.int}");

            properties.setProperty("remote-repo.type", "remote");
            properties.setProperty("remote-repo.uri", "http://localhost:${org.eclipse.virgo.repository.internal.test.int}/org.eclipse.virgo.repository/${org.eclipse.virgo.repository.internal.test.string}");
            properties.setProperty("remote-repo.indexRefreshInterval", "${org.eclipse.virgo.repository.internal.test.int}");

            properties.setProperty("chain", "bundles-ext,watched-repo,remote-repo");
            
            OrderedPair<Map<String,RepositoryConfiguration>,List<String>> configurations = configurationReader.readConfiguration(properties);
            Map<String,RepositoryConfiguration> configurationMap = configurations.getFirst();
            List<String> chainList = configurations.getSecond();

            assertEquals(3, chainList.size());
            assertEquals(3, configurationMap.size());
            
            ExternalStorageRepositoryConfiguration externalConfiguration = (ExternalStorageRepositoryConfiguration)configurationMap.get(chainList.get(0));
            assertEquals(new File(".").getAbsolutePath() + File.separator + "repository" + File.separator + "a" + File.separator + "ext" + File.separatorChar + "*.jar", externalConfiguration.getSearchPattern());
            
            WatchedStorageRepositoryConfiguration watchedConfiguration = (WatchedStorageRepositoryConfiguration)configurationMap.get(chainList.get(1));
            assertEquals(new File("build/repository", "a").getCanonicalFile(), watchedConfiguration.getDirectoryToWatch().getCanonicalFile());
            assertEquals(1, watchedConfiguration.getWatchInterval());
            
            RemoteRepositoryConfiguration remoteConfiguration = (RemoteRepositoryConfiguration)configurationMap.get(chainList.get(2));
            assertEquals(1, remoteConfiguration.getIndexUpdateInterval());
            assertEquals(URI.create("http://localhost:1/org.eclipse.virgo.repository/a"), remoteConfiguration.getRepositoryUri());
            
            assertEquals(0, eventLogger.getLoggedEvents().size());
        } finally {
            System.clearProperty("org.eclipse.virgo.repository.internal.test.string");
            System.clearProperty("org.eclipse.virgo.repository.internal.test.int");
        }
    }
}
