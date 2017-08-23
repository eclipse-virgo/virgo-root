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

package org.eclipse.virgo.repository.internal;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.RepositoryFactory;
import org.eclipse.virgo.repository.WatchableRepository;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.ManagedStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.WatchedStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.cacheing.CacheingRemoteRepository;
import org.eclipse.virgo.repository.internal.cacheing.cache.StandardRepositoryCacheFactory;
import org.eclipse.virgo.repository.internal.chain.ChainedRepository;
import org.eclipse.virgo.repository.internal.external.ExternalStorageRepository;
import org.eclipse.virgo.repository.internal.persistence.NoOpArtifactDescriptorPersister;
import org.eclipse.virgo.repository.internal.remote.RemoteRepository;
import org.eclipse.virgo.repository.internal.watched.WatchedStorageRepository;
import org.eclipse.virgo.util.common.Assert;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class StandardRepositoryFactory implements RepositoryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardRepositoryFactory.class);    

    private final EventLogger eventLogger;

    private final RepositoryDumpContributor dumpContributor;

    private final BundleContext bundleContext;

    private final ServiceRegistrationTracker tracker;

    StandardRepositoryFactory(EventLogger eventLogger, BundleContext bundleContext, ServiceRegistrationTracker tracker, RepositoryDumpContributor dumpContributor) {
        this.eventLogger = eventLogger;
        this.bundleContext = bundleContext;
        this.tracker = tracker;
        this.dumpContributor = dumpContributor;
    }    

    /**
     * {@inheritDoc}
     */
    public Repository createRepository(List<RepositoryConfiguration> repositoryConfigurations) throws RepositoryCreationException {
        Assert.notNull(repositoryConfigurations, "repositoryConfigurations cannot be null");
        Assert.notEmpty(repositoryConfigurations, "repositoryConfigurations cannot be empty");

        List<Repository> repositories = new ArrayList<Repository>();
        for (RepositoryConfiguration repositoryConfiguration : repositoryConfigurations) {
            try {
                Repository repository = createRepository(repositoryConfiguration);
                repositories.add(repository);
            } catch (RepositoryCreationException e) {
                // ignore -- event log already generated
            }
        }

        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < repositories.size(); i++) {
            if (i>0) { 
                nameBuilder.append("-");
            }
            nameBuilder.append(repositories.get(i).getName());
        }
        final String chainName = nameBuilder.toString();
        LOGGER.debug("Assembling chain named '{}' containing '{}' repository/ies.", chainName, repositories.size());
        return new ChainedRepository(chainName, repositories);
    }

    /**
     * {@inheritDoc}
     */
    public Repository createRepository(RepositoryConfiguration repositoryConfiguration, ArtifactDescriptorPersister artifactDescriptorPersister) throws RepositoryCreationException {
        Assert.notNull(repositoryConfiguration, "repositoryConfiguration cannot be null");

        Class<? extends RepositoryConfiguration> configurationClass = repositoryConfiguration.getClass();

        BaseRepository repository;
        final String repositoryName = repositoryConfiguration.getName();

        try {
            if (configurationClass.equals(ExternalStorageRepositoryConfiguration.class)) {
                repository = createExternalRepository(repositoryConfiguration, artifactDescriptorPersister);
            } else if (configurationClass.equals(WatchedStorageRepositoryConfiguration.class)) {
                repository = createWatchedRepository(repositoryConfiguration, artifactDescriptorPersister);
            } else if (configurationClass.equals(RemoteRepositoryConfiguration.class)) {
                repository = createRemoteRepository(repositoryConfiguration);
            } else if (configurationClass.equals(ManagedStorageRepositoryConfiguration.class)) {
                throw new RepositoryCreationException("Managed storage repositories are currently not supported");
            } else {
                throw new RepositoryCreationException(String.format("'%s' is an unrecognised type of RepositoryConfiguration", configurationClass));
            }
        } catch (RepositoryCreationException e) {
            eventLogger.log(RepositoryLogEvents.REPOSITORY_NOT_CREATED, repositoryName);
            throw e;
        }

        if (this.dumpContributor != null) {
            this.dumpContributor.addDepository(repository.getName(), repository.getDepository());
        }
       
        repository.start();
        return repository;
    }

    private RemoteRepository createRemoteRepository(RepositoryConfiguration repositoryConfiguration) throws RepositoryCreationException {
        try {
            return new CacheingRemoteRepository((RemoteRepositoryConfiguration) repositoryConfiguration, eventLogger,
                new StandardRepositoryCacheFactory());
        } catch (Exception e) {
            throw new RepositoryCreationException("Failed to create remote repository '" + repositoryConfiguration.getName() + "'.", e);
        }
    }

    private final LocalRepository createWatchedRepository(RepositoryConfiguration repositoryConfiguration, ArtifactDescriptorPersister artifactDescriptorPersister) throws RepositoryCreationException {
        try {
            WatchedStorageRepository watchableRepository;
            if (artifactDescriptorPersister==null) {
                watchableRepository = new WatchedStorageRepository((WatchedStorageRepositoryConfiguration) repositoryConfiguration, new NoOpArtifactDescriptorPersister(), eventLogger);
            } else {
                watchableRepository = new WatchedStorageRepository((WatchedStorageRepositoryConfiguration) repositoryConfiguration, artifactDescriptorPersister, eventLogger);
            }
            this.tracker.track(this.bundleContext.registerService(WatchableRepository.class, watchableRepository, null));
            return watchableRepository;
        } catch (Exception e) {
            throw new RepositoryCreationException("Failed to create watched repository '" + repositoryConfiguration.getName() + "'.", e);
        }
    }

    private final LocalRepository createExternalRepository(RepositoryConfiguration repositoryConfiguration, ArtifactDescriptorPersister artifactDescriptorPersister) throws RepositoryCreationException {
        try {
            if (artifactDescriptorPersister==null) {
                return new ExternalStorageRepository((ExternalStorageRepositoryConfiguration) repositoryConfiguration, eventLogger);
            } else {
                return new ExternalStorageRepository((ExternalStorageRepositoryConfiguration) repositoryConfiguration, artifactDescriptorPersister, eventLogger);
            }
        } catch (Exception e) {
            throw new RepositoryCreationException("Failed to create external repository '" + repositoryConfiguration.getName() + "'.", e);
        }
    }

    public Repository createRepository(RepositoryConfiguration repositoryConfiguration) throws RepositoryCreationException {
        return createRepository(repositoryConfiguration, null);
    }
}
