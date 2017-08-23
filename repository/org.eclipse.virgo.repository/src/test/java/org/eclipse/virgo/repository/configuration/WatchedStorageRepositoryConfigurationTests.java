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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.configuration.WatchedStorageRepositoryConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class WatchedStorageRepositoryConfigurationTests {

    private WatchedStorageRepositoryConfiguration configuration;

    private final static String name = "Watched-Repo-Name";

    private Set<ArtifactBridge> artefactBridges;

    private File watchedDirectory;

    private static final int BAD_WATCH_INTERVAL_NEG = -1;

    private static final int BAD_WATCH_INTERVAL_ZERO = 0;

    private static final int watchInterval = 5;

    @Before
    public void createConfiguration() {
        this.artefactBridges = new HashSet<ArtifactBridge>();
        this.artefactBridges.add(new ArtifactBridge() {

            public ArtifactDescriptor generateArtifactDescriptor(File artifact) throws ArtifactGenerationException {
                return null;
            }
        });
        this.watchedDirectory = new File("build/dir-to-watch");
        this.watchedDirectory.delete();
    }

    @Test
    public void name() {
        this.configuration = new WatchedStorageRepositoryConfiguration(name, new File("build/watchedIndex"), this.artefactBridges, this.watchedDirectory.getAbsolutePath(), watchInterval, null);
        assertEquals(name, this.configuration.getName());
    }

    @Test
    public void watchedDirectory() {
        this.watchedDirectory.mkdir();
        this.configuration = new WatchedStorageRepositoryConfiguration(name, new File("build/watchedIndex"), this.artefactBridges, this.watchedDirectory.getAbsolutePath(), watchInterval, null);
        assertEquals(this.watchedDirectory.getAbsoluteFile(), this.configuration.getDirectoryToWatch().getAbsoluteFile());
    }

    @Test
    public void artefactBridges() {
        this.configuration = new WatchedStorageRepositoryConfiguration(name, new File("build/watchedIndex"), this.artefactBridges, this.watchedDirectory.getAbsolutePath(), watchInterval, null);
        assertEquals(this.artefactBridges, this.configuration.getArtefactBridges());
    }

    @Test
    public void watchInterval() {
        this.configuration = new WatchedStorageRepositoryConfiguration(name, new File("build/watchedIndex"), this.artefactBridges, this.watchedDirectory.getAbsolutePath(), watchInterval, null);
        assertEquals(watchInterval, this.configuration.getWatchInterval());
    }

    @Test(expected = IllegalArgumentException.class)
    public void badWatchIntervalZero() {
        this.configuration = new WatchedStorageRepositoryConfiguration(name, new File("build/watchedIndex"), this.artefactBridges, this.watchedDirectory.getAbsolutePath(), BAD_WATCH_INTERVAL_ZERO,
            null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badWatchIntervalNeg() {
        this.configuration = new WatchedStorageRepositoryConfiguration(name, new File("build/watchedIndex"), this.artefactBridges, this.watchedDirectory.getAbsolutePath(), BAD_WATCH_INTERVAL_NEG,
            null);
    }

    @After
    public void cleanUp() {
        if (this.watchedDirectory.exists()) {

            if (this.watchedDirectory.isDirectory()) {
                for (File file : this.watchedDirectory.listFiles()) {
                    file.delete();
                }
            }
            this.watchedDirectory.delete();
        }
    }
}
