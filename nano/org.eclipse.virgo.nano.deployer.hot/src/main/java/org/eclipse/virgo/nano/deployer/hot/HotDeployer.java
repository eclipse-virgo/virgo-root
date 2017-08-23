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

package org.eclipse.virgo.nano.deployer.hot;

import java.io.File;

import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
import org.eclipse.virgo.nano.deployer.api.core.DeployerConfiguration;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.io.FileSystemChecker;
import org.eclipse.virgo.util.io.PathReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles hot deployment of application artefacts.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class HotDeployer {

    private static final String EXCLUDE_PATTERN = "\\.DS_Store|\\.state";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lifecycleLock = new Object();

    private final File pickupDir;
    
    private final Thread thread;

    /**
     * Creates a new <code>HotDeployer</code>.
     * 
     * @param deployerConfiguration the {@link DeployerConfiguration} parameters.
     * @param deployer the {@link ApplicationDeployer} to deploy to.
     * @param eventLogger where to log events
     */
    public HotDeployer(@NonNull DeployerConfiguration deployerConfiguration, @NonNull ApplicationDeployer deployer, EventLogger eventLogger) {
        this.pickupDir = createHotDeployDir(deployerConfiguration.getDeploymentPickupDirectory());
        FileSystemChecker checker = createFileSystemChecker(deployer, eventLogger);
        this.thread = new Thread(new WatchTask(checker, this.pickupDir, deployerConfiguration.getScanIntervalMillis()), "fs-watcher");
    }

	private FileSystemChecker createFileSystemChecker(ApplicationDeployer deployer, EventLogger eventLogger) {
		FileSystemChecker checker = new FileSystemChecker(this.pickupDir, EXCLUDE_PATTERN, this.logger);
        checker.addListener(new HotDeploymentFileSystemListener(deployer, eventLogger));
		return checker;
	}

    /**
     * Creates the hot deployment directory.
     * 
     * @param pickUpDirectoryPath the {@link PathReference} location of the pickup directory.
     * @return the {@link File} of the hot deployment directory.
     */
    private File createHotDeployDir(@NonNull PathReference pickUpDirectoryPath) {
        if (pickUpDirectoryPath.isFile()) {
            logger.debug("Deleting stray file from hot deployment directory location '{}'.", pickUpDirectoryPath.getAbsolutePath());
            pickUpDirectoryPath.delete();
        }
        if (!pickUpDirectoryPath.exists()) {
            logger.info("Creating hot deployment directory at '{}'.", pickUpDirectoryPath.getAbsolutePath());
            pickUpDirectoryPath.createDirectory();
        } else {
            logger.info("Using hot deployment directory at '{}'.", pickUpDirectoryPath.getAbsolutePath());
        }
        return pickUpDirectoryPath.toFile();
    }

    /**
     * Start the <code>FileSystemWatcher</code>.
     */
    public void doStart() {
        synchronized (this.lifecycleLock) {
            if (this.thread != null) {
                this.thread.start();
                logger.info("Started hot deployer on '{}'.", this.pickupDir);
            }
        }
    }

    /**
     * Stop the <code>FileSystemWatcher</code>,
     */
    public void doStop() {
        synchronized (this.lifecycleLock) {
            if (this.thread != null) {
                logger.info("Stopping hot deployer");
                this.thread.interrupt();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Hot Deployer [pickupDir = %s]", this.pickupDir.getAbsolutePath());
    }
}
