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

package org.eclipse.virgo.kernel.services.repository.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.FactoryBean;

import org.eclipse.virgo.kernel.services.internal.KernelServicesLogEvents;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.RepositoryFactory;
import org.eclipse.virgo.repository.configuration.PropertiesRepositoryConfigurationReader;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RepositoryConfigurationException;
import org.eclipse.virgo.util.math.OrderedPair;

public final class RepositoryFactoryBean implements FactoryBean<Repository> {

    private final Properties repositoryProperties;

    private final EventLogger eventLogger;

    private final RepositoryFactory repositoryFactory;

    private volatile Repository repository;

    private final File workDirectory;

    private final Set<ArtifactBridge> artifactBridges;

    private final String mBeanDomain;

    public RepositoryFactoryBean(Properties repositoryProperties, EventLogger eventLogger, RepositoryFactory repositoryFactory, File workDirectory,
        Set<ArtifactBridge> artifactBridges, String mBeanDomain) {
        this.repositoryProperties = repositoryProperties;
        this.eventLogger = eventLogger;
        this.repositoryFactory = repositoryFactory;
        this.workDirectory = workDirectory;
        this.artifactBridges = artifactBridges;
        this.mBeanDomain = mBeanDomain;
    }

    public Repository getObject() throws Exception {
        if (this.repository == null) {
            this.repository = createRepository();
        }
        return this.repository;
    }

    public Class<? extends Repository> getObjectType() {
        return Repository.class;
    }

    public boolean isSingleton() {
        return true;
    }
    
    public void destroy() {
        Repository localRepository = this.repository;
        if (localRepository != null) {
            this.repository = null;
            localRepository.stop();
        }
    }

    private Repository createRepository() throws RepositoryConfigurationException, RepositoryCreationException {

        PropertiesRepositoryConfigurationReader configurationReader = new PropertiesRepositoryConfigurationReader(this.workDirectory,
            this.artifactBridges, this.eventLogger, this.mBeanDomain);

        OrderedPair<Map<String, RepositoryConfiguration>, List<String>> configurations = configurationReader.readConfiguration(this.repositoryProperties);
        Map<String, RepositoryConfiguration> configurationMap = configurations.getFirst();
        List<String> chainList = configurations.getSecond();

        if (chainList.isEmpty()) {
            this.eventLogger.log(KernelServicesLogEvents.KERNEL_REPOSITORY_CHAIN_EMPTY);
            return new EmptyRepository(); // no chain
        }

        List<RepositoryConfiguration> repositoryConfigurationChain = new ArrayList<RepositoryConfiguration>(chainList.size());
        for (String repositoryName : chainList) {
            RepositoryConfiguration repositoryConfiguration = configurationMap.get(repositoryName);

            if (repositoryConfiguration == null) {
                this.eventLogger.log(KernelServicesLogEvents.KERNEL_REPOSITORY_CHAIN_ENTRY_NOT_VALID, repositoryName);
                throw new RepositoryConfigurationException("Cannot resolve repository '" + repositoryName + "' in chain.");
            }
            repositoryConfigurationChain.add(repositoryConfiguration);
        }
        return repositoryFactory.createRepository(repositoryConfigurationChain);
    }
}
