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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.ManagedStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.WatchedStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.RepositoryDumpContributor;
import org.eclipse.virgo.repository.internal.StandardRepositoryFactory;
import org.eclipse.virgo.repository.internal.chain.ChainedRepository;
import org.eclipse.virgo.repository.internal.external.ExternalStorageRepository;
import org.eclipse.virgo.repository.internal.remote.RemoteRepository;
import org.eclipse.virgo.repository.internal.watched.WatchedStorageRepository;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;

public class StandardRepositoryFactoryTests {

    MockEventLogger mockEventLogger = new MockEventLogger();

    StandardRepositoryFactory factory = new StandardRepositoryFactory(mockEventLogger, new StubBundleContext(), new ServiceRegistrationTracker(), new RepositoryDumpContributor(new XMLRepositoryCodec()));

    @Test
    public void chainedRepository() throws RepositoryCreationException {
        RepositoryConfiguration config = new ExternalStorageRepositoryConfiguration("test", new File("build/externalIndex"),
            Collections.<ArtifactBridge> emptySet(), System.getProperty("user.dir") + "/target/external", null);
        Repository repository = factory.createRepository(Arrays.asList(config, config));
        assertTrue(repository instanceof ChainedRepository);
        assertEquals("test-test", repository.getName());
        assertFalse("log events were issued", mockEventLogger.getCalled());
    }

    @Test
    public void chainedRepositoryWithBadRepositoryInChain() throws RepositoryCreationException {
        RepositoryConfiguration config = new ExternalStorageRepositoryConfiguration("test", new File("build/externalIndex"),
            Collections.<ArtifactBridge> emptySet(), System.getProperty("user.dir") + "/target/external", null);
        RepositoryConfiguration badConfig = new ManagedStorageRepositoryConfiguration("test", new File("build/managedIndex"),
            Collections.<ArtifactBridge> emptySet(), new File("build/managedStorage"), null);
        Repository repository = factory.createRepository(Arrays.asList(config, badConfig, config));
        assertTrue(repository instanceof ChainedRepository);
        assertEquals("test-test", repository.getName());
        assertTrue("log events not correctly issued", mockEventLogger.isLogged("RP0100W"));
    }

    @Test
    public void externalStorageRepository() throws RepositoryCreationException {
        Repository repository = factory.createRepository(new ExternalStorageRepositoryConfiguration("test", new File("build/externalIndex"),
            Collections.<ArtifactBridge> emptySet(), System.getProperty("user.dir") + "/target/external", null));
        assertTrue(repository instanceof ExternalStorageRepository);
        assertFalse("log events were issued", mockEventLogger.getCalled());
    }

    @Test
    public void watchedStorageRepository() throws RepositoryCreationException {
        File watchDir = new File("build/watch");
        watchDir.mkdirs();
        Repository repository = factory.createRepository(new WatchedStorageRepositoryConfiguration("test", new File("build/watchedIndex"), Collections.<ArtifactBridge> emptySet(),
            watchDir.getAbsolutePath(), 1000, null));
        assertTrue(repository instanceof WatchedStorageRepository);
        assertFalse("log events were issued", mockEventLogger.getCalled());
    }

    @Test
    public void remoteRepository() throws RepositoryCreationException {
        Repository repository = factory.createRepository(new RemoteRepositoryConfiguration("test", new File("build/remoteIndex"),
            URI.create("http://localhost"), 1000, null, new File("build")));
        assertTrue(repository instanceof RemoteRepository);
        assertFalse("log events were issued", mockEventLogger.getCalled());
    }

    @Test(expected = RepositoryCreationException.class)
    public void managedStorageRepository() throws RepositoryCreationException {
        factory.createRepository(new ManagedStorageRepositoryConfiguration("test", new File("build/managedIndex"),
            Collections.<ArtifactBridge> emptySet(), new File("build/managedStorage"), null));
    }

    @Test(expected = RepositoryCreationException.class)
    public void unknownRepository() throws RepositoryCreationException {
        factory.createRepository(new StubRepositoryConfiguration());
    }

    private static class StubRepositoryConfiguration extends RepositoryConfiguration {

        StubRepositoryConfiguration() {
            super("test", null);
        }
    }
}
