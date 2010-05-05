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

import org.eclipse.virgo.repository.Repository;


/**
 * Configuration for a {@link Repository} that is a proxy for a remote repository.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class RemoteRepositoryConfiguration extends RepositoryConfiguration {

    private final File indexLocation;

    private final URI repositoryUri;

    private final int indexUpdateInterval;

    private final File cacheDirectory;

    /**
     * Creates configuration for a new <code>Repository</code> that is a proxy for a remote repository, with the remote
     * repository being accessible at the location identified by the supplied <code>repositoryUri</code>. The
     * <code>Repository</code> will have the supplied <code>name</code> and may write a local copy of the remote
     * repository's index to the supplied <code>indexLocation</code>.
     * 
     * @param name The name of the repository
     * @param indexLocation The location to which the repository should write its local copy of the index
     * @param repositoryUri The location of the remote repository
     * @param indexUpdateInterval The period, in seconds, between updates to the local copy of the remote index
     * @param mBeanDomain the domain name of the management beans registered for this repository; if null no MBeans are
     *        registered
     * @param cacheDirectory the directory for cacheing remote artifacts
     */
    public RemoteRepositoryConfiguration(String name, File indexLocation, URI repositoryUri, int indexUpdateInterval, String mBeanDomain,
        File cacheDirectory) {
        super(name, mBeanDomain);
        this.indexLocation = indexLocation;
        this.repositoryUri = repositoryUri;
        this.indexUpdateInterval = indexUpdateInterval;
        this.cacheDirectory = cacheDirectory;
    }

    /**
     * Returns the location of the remote repository.
     * 
     * @return the remote repository's location.
     */
    public URI getRepositoryUri() {
        return this.repositoryUri;
    }

    /**
     * Returns the interval, in seconds, between updates to the local copy of the remote index.
     * 
     * @return the index update interval
     */
    public int getIndexUpdateInterval() {
        return this.indexUpdateInterval;
    }

    /**
     * Returns the location for this index to be written locally
     * 
     * @return the index location
     */
    public File getIndexLocation() {
        return this.indexLocation;
    }

    /**
     * Returns the directory to be used to store cached remote artifacts.
     * 
     * @return the cache directory
     */
    public File getCacheDirectory() {
        return this.cacheDirectory;
    }
}
