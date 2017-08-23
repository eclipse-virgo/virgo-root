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

package org.eclipse.virgo.repository.internal.chain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.repository.Query;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.internal.RepositoryUtils;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * <strong>Concurrent Semantics</strong><br />
 * 
 * As thread-safe as the repositories in the chain
 * 
 */
public final class ChainedRepository implements Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChainedRepository.class);

    private final List<Repository> repositories;

    private final String name;

    public ChainedRepository(String name, List<Repository> repositories) {
        this.name = name;
        this.repositories = repositories;
    }

    /**
     * {@inheritDoc}
     */
    public Query createQuery(String key, String value) {
        List<Query> queries = new ArrayList<Query>();
        for (Repository repository : this.repositories) {
            queries.add(repository.createQuery(key, value));
        }
        return new ChainedQuery(queries);
    }

    /**
     * {@inheritDoc}
     */
    public Query createQuery(String key, String value, Map<String, Set<String>> properties) {
        List<Query> queries = new ArrayList<Query>();
        for (Repository repository : this.repositories) {
            queries.add(repository.createQuery(key, value, properties));
        }
        return new ChainedQuery(queries);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public RepositoryAwareArtifactDescriptor get(String type, String name, VersionRange versionRange) {
        Set<RepositoryAwareArtifactDescriptor> artifacts = new HashSet<RepositoryAwareArtifactDescriptor>();

        for (Repository repository : this.repositories) {
            RepositoryAwareArtifactDescriptor artifact = repository.get(type, name, versionRange);
            if (artifact != null) {
                artifacts.add(artifact);
            }
        }
        LOGGER.debug("Chain returned {} artifacts named '{}' in version range '{}'.", new Object[] { artifacts.size(), name, versionRange });
        return RepositoryUtils.selectHighestVersion(artifacts);
    }

    public void stop() {
        for (Repository repository : this.repositories) {
            repository.stop();
        }
    }

}
