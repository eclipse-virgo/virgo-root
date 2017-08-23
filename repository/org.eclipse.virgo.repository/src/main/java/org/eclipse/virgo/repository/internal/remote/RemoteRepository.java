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

package org.eclipse.virgo.repository.internal.remote;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.BaseRepository;
import org.eclipse.virgo.repository.internal.RepositoryLogEvents;
import org.eclipse.virgo.repository.internal.management.StandardRemoteRepositoryInfo;
import org.eclipse.virgo.repository.management.RepositoryInfo;

/**
 * Implements a remotely hosted repository.
 * <p/>
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public class RemoteRepository extends BaseRepository {

    private static final String SCHEME_HTTP = "http";

	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteRepository.class);
	
	private final ArtifactDescriptorStoreWatcher watcher;
	
	private final File cacheDirectory;

    public RemoteRepository(RemoteRepositoryConfiguration configuration, EventLogger eventLogger) {
        this(configuration, new MutableArtifactDescriptorDepository(configuration.getName(), eventLogger), eventLogger);
    }

    private RemoteRepository(RemoteRepositoryConfiguration configuration, MutableArtifactDescriptorDepository depository, EventLogger eventLogger) {
        super(configuration, depository);
        this.cacheDirectory = configuration.getCacheDirectory();

        if (!SCHEME_HTTP.equals(configuration.getRepositoryUri().getScheme())) {
            LOGGER.error("Uri '{}' scheme not http for remote repository '{}'.", configuration.getRepositoryUri(), getName());
            eventLogger.log(RepositoryLogEvents.REPOSITORY_NOT_CREATED, configuration.getName());
            throw new IllegalArgumentException("Proxy only supports http");
        }

        watcher = new ArtifactDescriptorStoreWatcher(depository, configuration);        
    }
    
    @Override
	protected void start() {
		super.start();
		this.watcher.start();
	}

	@Override
	public void stop() {		
		super.stop();
		this.watcher.stop();
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected RepositoryInfo createMBean() {
        return new StandardRemoteRepositoryInfo(getName(), getDepository());
    }
    
    /**
     * Returns the directory for cacheing remote artifacts.
     * 
     * @return the cache directory
     */
    public File getCacheDirectory() {
        return this.cacheDirectory;
    }
}
