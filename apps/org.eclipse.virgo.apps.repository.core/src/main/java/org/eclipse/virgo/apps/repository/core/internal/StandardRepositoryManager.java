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

package org.eclipse.virgo.apps.repository.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.virgo.apps.repository.core.RepositoryIndex;
import org.eclipse.virgo.apps.repository.core.RepositoryManager;
import org.eclipse.virgo.kernel.services.work.WorkArea;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.gemini.web.core.ConnectorDescriptor;
import org.eclipse.gemini.web.core.WebContainerProperties;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.RepositoryFactory;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.configuration.PersistentRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.PropertiesRepositoryConfigurationReader;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RepositoryConfigurationException;
import org.eclipse.virgo.util.math.OrderedPair;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
class StandardRepositoryManager implements RepositoryManager {

    private final EventLogger eventLogger;

    private final Map<String, HostedRepository> repositories = new HashMap<String, HostedRepository>();

    StandardRepositoryManager(Properties configuration, RepositoryFactory repositoryFactory, WorkArea workArea, Set<ArtifactBridge> artifactBridges, String repositoryManagementDomain, HostedRepositoryObjectNameFactory objectNameFactory, WebContainerProperties webContainerProperties, EventLogger eventLogger) {
        this(formConfigurationMap(configuration, workArea, artifactBridges, repositoryManagementDomain, eventLogger), repositoryFactory,
            objectNameFactory, webContainerProperties, eventLogger);
    }
    
    StandardRepositoryManager(Map<String, RepositoryConfiguration> configurationMap, RepositoryFactory repositoryFactory, HostedRepositoryObjectNameFactory objectNameFactory, WebContainerProperties webContainerProperties, EventLogger eventLogger) {
        
        this.eventLogger = eventLogger;
        int port = determineHttpPort(webContainerProperties);
        checkPersistentRepositoryConfigurations(configurationMap.values());

        for (Entry<String, RepositoryConfiguration> mapEntry : configurationMap.entrySet()) {
            String repositoryName = mapEntry.getKey();
            
            RepositoryConfiguration repositoryConfiguration = mapEntry.getValue();
            if (repositoryConfiguration instanceof PersistentRepositoryConfiguration) {
                PersistentRepositoryConfiguration persistentRepositoryConfiguration = (PersistentRepositoryConfiguration) repositoryConfiguration;
                try {
                    HostedRepositoryUriMapper uriMapper = new HostedRepositoryUriMapper(port, repositoryName);
                    persistentRepositoryConfiguration.setUriMapper(uriMapper);
                    ExportableIndexFilePool filePool = new ExportableIndexFilePool(persistentRepositoryConfiguration.getIndexLocation(), repositoryName);
                    ExportingArtifactDescriptorPersister artifactDescriptorPersister = 
                        new LazyExportableXMLArtifactDescriptorPersister(repositoryName, new XMLRepositoryCodec(), filePool);
                    Repository repository = repositoryFactory.createRepository(persistentRepositoryConfiguration, artifactDescriptorPersister);
                    HostedRepository hostedRepository = new HostedRepository(repository, artifactDescriptorPersister, uriMapper, objectNameFactory);

                    repositories.put(repositoryName, hostedRepository);
                } catch (UnknownHostException uhe) {
                    eventLogger.log(HostedRepositoryLogEvents.HOST_ADDRESS_EXCEPTION, uhe, repositoryName);
                } catch (RepositoryCreationException rce) {
                    eventLogger.log(HostedRepositoryLogEvents.REPOSITORY_EXCEPTION, rce, repositoryName);
                }
            }
        }
    }

    private static final Map<String, RepositoryConfiguration> formConfigurationMap(Properties configuration, WorkArea workArea,
        Set<ArtifactBridge> artifactBridges, String repositoryManagementDomain, EventLogger eventLogger) {
        Set<ArtifactBridge> copySet = new HashSet<ArtifactBridge>();
        copySet.addAll(artifactBridges);

        PropertiesRepositoryConfigurationReader configurationReader = new PropertiesRepositoryConfigurationReader(
            workArea.getWorkDirectory().toFile(), copySet, eventLogger, repositoryManagementDomain);
        OrderedPair<Map<String, RepositoryConfiguration>, List<String>> configurations;
        try {
            configurations = configurationReader.readConfiguration(configuration);
            checkNoChainDefined(configurations, eventLogger);
            return configurations.getFirst();
        } catch (RepositoryConfigurationException rce) {
            eventLogger.log(HostedRepositoryLogEvents.CONFIGURATION_EXCEPTION, rce);
        }
        return null;
    }

    private static final void checkNoChainDefined(OrderedPair<Map<String, RepositoryConfiguration>, List<String>> configurations,
        EventLogger eventLogger) {
        if (!configurations.getSecond().isEmpty()) {
            final List<String> chainList = configurations.getSecond();
            String chainListString = chainList.get(0);
            for (int i = 1; i < chainList.size(); ++i) {
                chainListString += "," + chainList.get(i);
            }
            eventLogger.log(HostedRepositoryLogEvents.CHAIN_NON_EMPTY, chainListString);
        }
    }

    private final void checkPersistentRepositoryConfigurations(Collection<RepositoryConfiguration> configurationSet) {
        for (RepositoryConfiguration repositoryConfiguration : configurationSet) {
            if (!(repositoryConfiguration instanceof PersistentRepositoryConfiguration)) {
                this.eventLogger.log(HostedRepositoryLogEvents.NON_SUPPORTED_REPOSITORY, repositoryConfiguration.getName());
            }
        }
    }

    private final static int determineHttpPort(final WebContainerProperties webContainerProperties) {
        Set<ConnectorDescriptor> connectorDescriptors = webContainerProperties.getConnectorDescriptors();
        int result = -1;
        for(ConnectorDescriptor connectorDescriptor : connectorDescriptors) {
            if(connectorDescriptor.sslEnabled() == false && connectorDescriptor.getScheme().contains("http")) {
                result = connectorDescriptor.getPort();
            }
        }
        return result;
    }

    /**
     * Start the hosted repositories successfully constructed
     */
    void start() {
        for (Entry<String, HostedRepository> entry : this.repositories.entrySet()) {
            entry.getValue().start();
        }
    }

    /**
     * Stop the hosted repositories successfully constructed
     */
    void stop() {
        for (Entry<String, HostedRepository> entry : this.repositories.entrySet()) {
            entry.getValue().stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getArtifact(String repositoryName, String type, String name, String version) {
        HostedRepository hostedRepository = this.repositories.get(repositoryName);
        if (hostedRepository != null) {
            try {
                URI originalUri = hostedRepository.inverseMapping(type, name, version);
                return null == originalUri ? null : originalUri.toURL().openStream();
            } catch (IOException ioe) {
                return null;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    public RepositoryIndex getIndex(String repositoryName) throws IOException {
        HostedRepository hostedRepository = this.repositories.get(repositoryName);
        if (hostedRepository != null) {
            return hostedRepository.getRepositoryIndex();
        }
        return null;
    }
}
