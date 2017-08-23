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

package org.eclipse.virgo.repository;

import java.util.List;

import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;


/**
 * A factory for creating {@link Repository Repositories}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations <strong>must</strong> be thread-safe.
 *
 */
public interface RepositoryFactory {
    
    /**
     * Creates a new <code>Repository</code> from the provided list of <code>RepositoryConfigurations</code>.
     * The created <code>Repository</code> will be backed by a chain of repositories, the ordering of which
     * is determined by the ordering of the configuration entries in the supplied list.
     * 
     * @param repositoryConfigurations The list of configuration from which the chain will be created
     * @return The chained <code>Repository</code>.
     * @throws RepositoryCreationException 
     */
    Repository createRepository(List<RepositoryConfiguration> repositoryConfigurations) throws RepositoryCreationException;
    
    /**
     * Creates a new <code>Repository</code> from the provided <code>RepositoryConfiguration</code>.
     * 
     * @param repositoryConfiguration The configuration from which the chain will be created
     * @return The <code>Repository</code>.
     * @throws RepositoryCreationException 
     */
    Repository createRepository(RepositoryConfiguration repositoryConfiguration) throws RepositoryCreationException;
    
    /**
     * Creates a new <code>Repository</code> from the provided <code>RepositoryConfiguration</code> with a customised persister.
     * 
     * @param repositoryConfiguration The configuration from which the chain will be created
     * @param artifactDescriptorPersister A persister to use for the artifact depository
     * @return The <code>Repository</code>.
     * @throws RepositoryCreationException 
     */
    Repository createRepository(RepositoryConfiguration repositoryConfiguration, ArtifactDescriptorPersister artifactDescriptorPersister) throws RepositoryCreationException;
    
}
