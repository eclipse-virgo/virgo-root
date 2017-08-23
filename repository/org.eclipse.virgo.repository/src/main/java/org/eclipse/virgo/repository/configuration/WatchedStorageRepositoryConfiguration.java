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
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.UriMapper;
import org.eclipse.virgo.repository.internal.IdentityUriMapper;


/**
 * Configuration for a {@link Repository} that is populated by watching a directory.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class WatchedStorageRepositoryConfiguration extends PersistentRepositoryConfiguration {

    private final File directoryToWatch;

    /**
     * Period, in seconds, to wait between checking the <code>directoryToWatch</code>
     */
    private final int watchInterval;

    /**
     * Creates configuration for a new <code>Repository</code> with watched storage. The <code>Repository</code> will
     * have the supplied <code>name</name> and will write its index to the supplied
     * <code>indexLocation</code>. The <code>Repository</code> will watch the supplied <code>directory</code> adding
     * artifacts as files are added to the directory, and removing artifacts as files are removed from the directory.
     * The directory will be checked at the supplied <code>watchInterval</code>, measured in seconds.
     * 
     * @param name The name of the repository
     * @param indexLocation Location of persistence indexes if used
     * @param artifactBridges The artifact bridges to be used to generate artifacts when items are added to the
     *        repository
     * @param directoryToWatch The path of a directory to be watched
     * @param watchInterval The period, in seconds, between checks of the watched directory; must be > 0.
     * @param mBeanDomain domain name of management beans registered -- none registered if null
     */
    public WatchedStorageRepositoryConfiguration(String name, File indexLocation, Set<ArtifactBridge> artifactBridges, String directoryToWatch, int watchInterval,
        String mBeanDomain) {
        this(name, indexLocation, artifactBridges, directoryToWatch, watchInterval, new IdentityUriMapper(), mBeanDomain);
    }

    /**
     * Creates configuration for a new <code>Repository</code> with watched storage. The <code>Repository</code> will
     * have the supplied <code>name</name> and will write its index to the supplied
     * <code>indexLocation</code>. The <code>Repository</code> will watch the supplied <code>directory</code> adding
     * artifacts as files are added to the directory, and removing artifacts as files are removed from the directory.
     * The directory will be checked at the supplied <code>watchInterval</code>, measured in seconds.
     * 
     * @param name The name of the repository
     * @param indexLocation location of persistent indexes if any
     * @param artifactBridges The artifact bridges to be used to generate artifacts when items are added to the
     *        repository
     * @param directoryToWatch The path of a directory to be watched
     * @param watchInterval The period, in seconds, between checks of the watched directory; must be > 0.
     * @param uriMapper used to map URIs stored in the repository's index
     * @param mBeanDomain domain name of management beans registered -- none registered if null
     */
    public WatchedStorageRepositoryConfiguration(String name, File indexLocation, Set<ArtifactBridge> artifactBridges, String directoryToWatch, int watchInterval,
        UriMapper uriMapper, String mBeanDomain) {
        super(name, indexLocation, artifactBridges, uriMapper, mBeanDomain);
        this.directoryToWatch = validateWatchDirectory(directoryToWatch);
        if (watchInterval <= 0) {
            throw new IllegalArgumentException("watch interval (" + watchInterval + ") must be 1 or more seconds");
        }
        this.watchInterval = watchInterval;
    }

    private File validateWatchDirectory(String watchDirPath) {
        if (watchDirPath==null) {
            throw new IllegalArgumentException("watch directory path must not be null");
        } else {
            try {
                File wdFile = new File(watchDirPath);
                if (!wdFile.exists()) return wdFile;
                if (wdFile.isDirectory()) return wdFile;
            } catch (Exception e) { }
        }
        throw new IllegalArgumentException("watch directory path '" + watchDirPath + "' is not a valid directory path");
    }

    /**
     * Returns the directory to be watched
     * 
     * @return the watched directory
     */
    public File getDirectoryToWatch() {
        return this.directoryToWatch;
    }

    /**
     * Returns the interval, in seconds, between checks of the watched directory
     * 
     * @return the watch interval
     */
    public int getWatchInterval() {
        return this.watchInterval;
    }
}
