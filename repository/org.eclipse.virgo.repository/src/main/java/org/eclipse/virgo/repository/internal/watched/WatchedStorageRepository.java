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

package org.eclipse.virgo.repository.internal.watched;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.WatchableRepository;
import org.eclipse.virgo.repository.configuration.WatchedStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.LocalRepository;
import org.eclipse.virgo.repository.internal.RepositoryLogEvents;
import org.eclipse.virgo.repository.internal.management.StandardWatchedStorageRepositoryInfo;
import org.eclipse.virgo.repository.internal.persistence.NoOpArtifactDescriptorPersister;
import org.eclipse.virgo.repository.management.RepositoryInfo;
import org.eclipse.virgo.util.io.FileSystemChecker;
import org.eclipse.virgo.util.io.FileSystemEvent;
import org.eclipse.virgo.util.io.FileSystemListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Repository} that watches a local directory and automatically publishes and retracts artifacts placed (or
 * removed from) there.
 * 
 * <p>
 * <strong>Concurrent Semantics</strong><br/>
 * Thread-safe
 * </p>
 * 
 */
public final class WatchedStorageRepository extends LocalRepository implements WatchableRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WatchedStorageRepository.class);
    
    private static final String EXCLUDE_PATTERN = "\\.DS_Store";
    
    private final DirectoryWatcher dirWatcher; // monitors the watched directory
    
    private final ScheduledExecutorService executorService; // controls the watching threads
    
    private final int watchInterval;
    
    private final File watchDirectory;
    
    private final EventLogger eventLogger;
    
    public WatchedStorageRepository(WatchedStorageRepositoryConfiguration configuration, EventLogger eventLogger) throws RepositoryCreationException,
    IndexFormatException {
        this(configuration, new NoOpArtifactDescriptorPersister(), eventLogger);
    }
    
    public WatchedStorageRepository(WatchedStorageRepositoryConfiguration configuration, ArtifactDescriptorPersister artifactDescriptorPersister,
                                    EventLogger eventLogger) throws RepositoryCreationException, IndexFormatException {
        super(configuration, artifactDescriptorPersister, eventLogger);
        
        this.eventLogger = eventLogger;
        
        this.watchDirectory = configuration.getDirectoryToWatch();
        this.dirWatcher = new DirectoryWatcher(this.watchDirectory);
        this.watchInterval = configuration.getWatchInterval();
        
        // create thread pool for watching the directory, containing one daemon thread.
        this.executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
    }
    
    /**
     * Start the base repository and periodic checking.
     */
    @Override
    public void start() {
        super.start();
        LOGGER.info("Starting to watch directory '{}'; period {}s.", this.watchDirectory, this.watchInterval);
        
        // do initial check
        this.dirWatcher.fsChecker.check();
        // start periodic checking
        this.executorService.scheduleAtFixedRate(this.dirWatcher, this.watchInterval, this.watchInterval, TimeUnit.SECONDS);
    }
    
    /**
     * Stop watching the repository store and stop the base repository.
     */
    @Override
    public void stop() {
        LOGGER.info("Stopping watched directory '{}'.", this.watchDirectory);
        this.executorService.shutdown();
        super.stop();
    }
    
    /**
     * Private {@link Runnable}, an instance of which watches the directory for us
     */
    private final class DirectoryWatcher implements Runnable {
        
        private final FileSystemChecker fsChecker;
        
        private final FileSystemListener listener;
        
        private DirectoryWatcher(final File directory) throws RepositoryCreationException {
            establishDirectory(directory);
            
            this.fsChecker = new FileSystemChecker(directory, EXCLUDE_PATTERN);
            
            this.listener = new FileSystemListener() {
                
                @Override
                public void onChange(String path, FileSystemEvent event) {
                    File file = new File(path);
                    try {
                        LOGGER.debug("Listener for '{}' heard event '{}' on file '{}'.", new Object[] { WatchedStorageRepository.this.watchDirectory,
                            event, file });
                        switch (event) {
                            case CREATED:
                            case INITIAL: {
                                RepositoryAwareArtifactDescriptor artifactDescriptor = createArtifactDescriptor(file);
                                if (artifactDescriptor != null) {
                                    getDepository().addArtifactDescriptor(artifactDescriptor);
                                }
                                break;
                            }
                            case DELETED:
                                getDepository().removeArtifactDescriptor(file.toURI());
                                break;
                            case MODIFIED: {
                                getDepository().removeArtifactDescriptor(file.toURI());
                                RepositoryAwareArtifactDescriptor artifactDescriptor = createArtifactDescriptor(file);
                                if (artifactDescriptor != null) {
                                    getDepository().addArtifactDescriptor(artifactDescriptor);
                                }
                                break;
                            }
                        }
                        
                        getDepository().persist();
                    } catch (DuplicateArtifactException dae) {
                        LOGGER.warn("Duplicate artifact in file '{}' detected in watched directory '{}'.", file,
                                    WatchedStorageRepository.this.watchDirectory);
                    } catch (IOException e) {
                        LOGGER.error(String.format("Watched directory '%s' failed during persist. Stopping repository.",
                                                   WatchedStorageRepository.this.watchDirectory), e);
                        stop();
                        WatchedStorageRepository.this.eventLogger.log(RepositoryLogEvents.REPOSITORY_NOT_AVAILABLE, e, getName());
                    }
                }
                
                @Override
                public void onInitialEvent(List<String> paths) {
                    // no-op
                    // not available for watched repository
                    // only applicable for the pickup directory on a server's startup
                }
            };
            this.fsChecker.addListener(this.listener);
        }
        
        @Override
        public void run() {
            try {
                this.fsChecker.check();
            } catch (Exception e) {
                LOGGER.error("File system watcher for repository '{}' failed. Repository stopped.", getName());
                stop();
                WatchedStorageRepository.this.eventLogger.log(RepositoryLogEvents.REPOSITORY_NOT_AVAILABLE, e, getName());
            }
        }
        
        private final void establishDirectory(File dir) throws RepositoryCreationException {
            if (dir.exists()) {
                if (!dir.isDirectory()) {
                    if (!dir.delete()) {
                        LOGGER.error("Directory '{}' for watched repository '{}' is already a file and cannot be deleted. Repository unavailable.",
                                     dir.getName(), getName());
                        throw new RepositoryCreationException("Failed to delete index file for repository '" + getName() + "'");
                    } else {
                        LOGGER.debug("File '{}' deleted to create directory for watched repository '{}'.", dir, getName());
                    }
                }
            }
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    LOGGER.error("Directory '{}' for watched repository '{}' cannot be created. Repository unavailable.", dir.getName(), getName());
                    throw new RepositoryCreationException("Failed to delete index file for repository '" + getName() + "'");
                }
            }
        }
        
        /**
         * Run a check against the directory, accumulating new files completely.
         * 
         * @throws Exception anything that might escape from fs.check()
         */
        public void forceNewCheck() throws Exception {
            try {
                this.fsChecker.check();
                // The second check() is to force indexing of new files based on DirectoryWatcher.onChange()
                // implementation.
                this.fsChecker.check();
            } catch (Exception e) {
                LOGGER.warn("Directory check for repository '{}' failed.", getName());
                throw e;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected RepositoryInfo createMBean() throws JMException {
        return new StandardWatchedStorageRepositoryInfo(getName(), this.getDepository(), this);
    }
    
    /**
     * Performs a directory check upon the watched directory; equivalent to waiting for the directory file system
     * checker to run, but is synchronous.
     * 
     * @throws Exception from directory watcher
     */
    @Override
    public void forceCheck() throws Exception {
        this.dirWatcher.forceNewCheck();
    }
    
    @Override
    public Set<String> getArtifactLocations(String filename) {
        Set<String> locations = new HashSet<String>(1);
        locations.add(new File(this.watchDirectory, filename).getAbsolutePath());
        return locations;
    }
}
