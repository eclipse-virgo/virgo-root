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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.configuration.RemoteRepositoryConfiguration;
import org.eclipse.virgo.repository.configuration.StubRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.PersistentRepository;
import org.eclipse.virgo.repository.internal.RepositoryLogEvents;
import org.eclipse.virgo.repository.management.RepositoryInfo;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.io.NetUtils;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class RemoteRepositoryTests {

    private static final String MBEAN_DOMAIN_VIRGO_WEB_SERVER = "org.eclipse.virgo.server";

    private final MockEventLogger mockEventLogger = new MockEventLogger();

    private String indexEtag;

    private final Object repositoryLock = new Object();

    private int repositoryId = 0;

    private HttpServer httpServer;
    private int port;

    private final File indexLocation = new File("build/index");

    private final File proxyIndexLocation = new File("build/proxy.index");

    private final File cacheDirectory = new File("build");

    @Before
    public void deleteIndex() {
        this.indexLocation.delete();
        this.proxyIndexLocation.delete();
        this.port = NetUtils.getFreePort();
    }

    private void createRepository(ArtifactBridge artefactBridge, EventLogger eventLogger) throws Exception {
        this.indexLocation.delete();
        synchronized (this.repositoryLock) {
            StubRepository repository = new StubRepository(artefactBridge, this.indexLocation, eventLogger);
            repository.addArtifact(new File("artefact0"));
            repository.addArtifact(new File("artefact1"));
            repository.addArtifact(new File("artefact2"));
            repository.persist();

            this.indexEtag = Integer.toString(this.repositoryId++);
        }
    }

    private void bootstrapHttpServer(HttpHandler handler) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 30);
        this.httpServer.start();

        this.httpServer.createContext("/repository", handler);
    }

    private class StandardHttpHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {
            synchronized (RemoteRepositoryTests.this.repositoryLock) {
                boolean notModified = false;

                List<String> etags = exchange.getRequestHeaders().get("If-None-Match");

                if (etags != null) {
                    for (String etag : etags) {
                        if (RemoteRepositoryTests.this.indexEtag.equals(etag)) {
                            notModified = true;
                            break;
                        }
                    }
                }

                exchange.getResponseHeaders().set("Etag", RemoteRepositoryTests.this.indexEtag);

                if (notModified) {
                    exchange.sendResponseHeaders(304, 0);
                } else {
                    sendIndex(exchange);
                }
                exchange.getResponseBody().close();
            }
        }
    }

    private void sendIndex(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/org.eclipse.virgo.repository.Index");

        ByteArrayOutputStream indexStream = new ByteArrayOutputStream();
        FileInputStream input = new FileInputStream(RemoteRepositoryTests.this.indexLocation);
        FileCopyUtils.copy(input, indexStream);
        byte[] index = indexStream.toByteArray();
        exchange.sendResponseHeaders(200, index.length);
        exchange.getResponseBody().write(index);
    }

    @After
    public void shutdownHttpServer() {
        if (this.httpServer != null) {
            this.httpServer.stop(0);
        }
        this.indexLocation.delete();
    }

    @Test(timeout = 5 * 60 * 1000)
    public void queryAcrossUpdates() throws Exception {
        ArtifactBridge bridge = new StubArtifactBridge();
        createRepository(bridge, this.mockEventLogger);
        bootstrapHttpServer(new StandardHttpHandler());

        URI repositoryUri = URI.create("http://localhost:" + this .port + "/repository");
        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = createRemoteRepository(configuration, this.mockEventLogger);
        repository.start();

        int size = 0;
        Set<RepositoryAwareArtifactDescriptor> artifacts = null;
        while (size != 3) {
            artifacts = repository.createQuery("type", "dummy").run();
            size = artifacts.size();
        }

        for (RepositoryAwareArtifactDescriptor artifact : artifacts) {
            Version version = artifact.getVersion();

            if (version.getMajor() > 2) {
                fail("Unexpected version: " + version);
            }
        }

        createRepository(bridge, this.mockEventLogger);
        boolean success = true;

        do {
            artifacts = repository.createQuery("type", "dummy").run();
            if (artifacts.size() != 3) {
                success = false;
            } else {
                for (RepositoryAwareArtifactDescriptor artifact : artifacts) {
                    Version version = artifact.getVersion();

                    if (version.getMajor() < 3 || version.getMajor() > 5) {
                        success = false;
                        break;
                    }

                    success = true;
                }
            }
        } while (!success);

        repository.stop();

    }

    protected RemoteRepository createRemoteRepository(RemoteRepositoryConfiguration configuration, EventLogger eventLogger) {
        return new RemoteRepository(configuration, eventLogger);
    }

    @Test
    public void get() throws Exception {
        ArtifactBridge bridge = new StubArtifactBridge();
        createRepository(bridge, this.mockEventLogger);

        bootstrapHttpServer(new StandardHttpHandler());

        URI repositoryUri = URI.create("http://localhost:" + this .port + "/repository");
        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = new RemoteRepository(configuration, new MockEventLogger());

        repository.start();

        pollUntilDescriptorAvailable(repository, "dummy", "dummy", new VersionRange("[2,2]"));
        assertNull(repository.get("dummy", "dummy", new VersionRange("[3,4)")));

        repository.stop();
    }

    @Test
    public void repositoryAvailableMessageIsLoggedOnceRepositoryHasStarted() throws Exception {
        ArtifactBridge bridge = new StubArtifactBridge();
        createRepository(bridge, new MockEventLogger());
        bootstrapHttpServer(new StandardHttpHandler());

        URI repositoryUri = URI.create("http://localhost:" + this .port + "/repository");
        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = createRemoteRepository(configuration, this.mockEventLogger);
        repository.start();
        
        String eventCode = RepositoryLogEvents.REPOSITORY_AVAILABLE.getEventCode();
        while (!this.mockEventLogger.containsLogged(eventCode)) {
            Thread.sleep(50);
        }
    }

    @Test
    public void indexlessRepository() throws Exception {
        URI repositoryUri = URI.create("http://localhost:" + this .port + "/does-not-exist");

        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = new RemoteRepository(configuration, new MockEventLogger());

        repository.start();

        assertTrue(repository.createQuery("name", "foo").run().isEmpty());
        assertNull(repository.get("foo", "the-foo", VersionRange.NATURAL_NUMBER_RANGE));

        repository.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void badScheme() throws Exception {
        URI repositoryUri = URI.create("hotp://localhost:" + this .port + "/repository");

        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = new RemoteRepository(configuration, new MockEventLogger());
        assertFalse("Managed to create a badly formed RemoteRepository!", repository != null);
    }

    @Test(timeout = 5 * 60 * 1000)
    public void shorterThanExpectedIndex() throws Exception {
        ArtifactBridge bridge = new StubArtifactBridge();
        createRepository(bridge, this.mockEventLogger);
        final AtomicBoolean sendProperIndex = new AtomicBoolean(false);

        bootstrapHttpServer(new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {
                if (sendProperIndex.get()) {
                    sendIndex(exchange);
                } else {
                    long responseLength = 1234;
                    exchange.sendResponseHeaders(HttpStatus.SC_OK, responseLength);
                }
                exchange.getResponseBody().close();
            }
        });

        URI repositoryUri = URI.create("http://localhost:" + this .port + "/repository");
        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = new RemoteRepository(configuration, new MockEventLogger());
        repository.start();

        assertNull(repository.get("dummy", "dummy", new VersionRange("[2,2]")));

        createRepository(new StubArtifactBridge(), this.mockEventLogger);
        sendProperIndex.set(true);

        pollUntilDescriptorAvailable(repository, "dummy", "dummy", new VersionRange("[2,2]"));

        repository.stop();
    }

    void pollUntilDescriptorAvailable(Repository repository, String type, String name, VersionRange version) {
        while (repository.get(type, name, version) == null) {
        }
    }

    @Test(timeout = 5 * 60 * 1000)
    public void longerThanExpectedIndex() throws Exception {
        ArtifactBridge bridge = new StubArtifactBridge();
        createRepository(bridge, this.mockEventLogger);
        final AtomicBoolean sendProperIndex = new AtomicBoolean(false);

        bootstrapHttpServer(new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {
                if (sendProperIndex.get()) {
                    sendIndex(exchange);
                } else {
                    exchange.sendResponseHeaders(200, 4);
                    exchange.getResponseBody().write(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
                }
                exchange.getResponseBody().close();
            }
        });

        URI repositoryUri = URI.create("http://localhost:" + this .port + "/repository");
        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = new RemoteRepository(configuration, new MockEventLogger());
        repository.start();

        assertNull(repository.get("dummy", "dummy", new VersionRange("[2,2]")));

        createRepository(new StubArtifactBridge(), this.mockEventLogger);
        sendProperIndex.set(true);

        while (repository.get("dummy", "dummy", new VersionRange("[2,2]")) == null) {
        }

        repository.stop();
    }

    @Test(timeout = 5 * 60 * 1000)
    public void corruptedIndex() throws Exception {
        ArtifactBridge bridge = new StubArtifactBridge();
        createRepository(bridge, this.mockEventLogger);
        final AtomicBoolean sendProperIndex = new AtomicBoolean(false);

        bootstrapHttpServer(new HttpHandler() {

            public void handle(HttpExchange exchange) throws IOException {
                if (sendProperIndex.get()) {
                    sendIndex(exchange);
                } else {
                    byte[] indexBytes = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
                    exchange.sendResponseHeaders(200, indexBytes.length);
                    exchange.getResponseBody().write(indexBytes);
                }
                exchange.getResponseBody().close();
            }
        });

        URI repositoryUri = URI.create("http://localhost:" + this .port + "/repository");
        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = new RemoteRepository(configuration, new MockEventLogger());
        repository.start();

        assertNull(repository.get("dummy", "dummy", new VersionRange("[2,2]")));

        createRepository(new StubArtifactBridge(), this.mockEventLogger);
        sendProperIndex.set(true);

        while (repository.get("dummy", "dummy", new VersionRange("[2,2]")) == null) {
        }

        sendProperIndex.set(false);

        Thread.sleep(3000);

        repository.stop();
    }

    @Test
    public void mBeanPublication() throws Exception {
        URI repositoryUri = URI.create("http://localhost:" + this .port + "/repository");
        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            MBEAN_DOMAIN_VIRGO_WEB_SERVER, this.cacheDirectory);
        RemoteRepository repository = new RemoteRepository(configuration, new MockEventLogger());
        ObjectName objectName = new ObjectName(MBEAN_DOMAIN_VIRGO_WEB_SERVER + ":type=Repository,name=remote-repo");

        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present until repository has been started");
        } catch (InstanceNotFoundException infe) {
        }

        repository.start();

        MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(objectName);
        Object type = mBeanInfo.getDescriptor().getFieldValue("type");
        assertNotNull(type);
        assertEquals("remote", type);

        repository.stop();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present once repository has been stopped");
        } catch (InstanceNotFoundException infe) {
        }
    }

    @Test
    public void mBeanNonPublication() throws Exception {
        URI repositoryUri = URI.create("http://localhost:" + this .port + "/repository");
        RemoteRepositoryConfiguration configuration = new RemoteRepositoryConfiguration("remote-repo", this.proxyIndexLocation, repositoryUri, 1,
            null, this.cacheDirectory);
        RemoteRepository repository = new RemoteRepository(configuration, new MockEventLogger());
        ObjectName objectName = new ObjectName(MBEAN_DOMAIN_VIRGO_WEB_SERVER + ":type=Repository,name=remote-repo");
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present before start");
        } catch (InstanceNotFoundException infe) {
        }

        repository.start();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present after start");
        } catch (InstanceNotFoundException infe) {
        }

        repository.stop();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present once repository has been stopped");
        } catch (InstanceNotFoundException infe) {
        }
    }

    private static class StubArtifactBridge implements ArtifactBridge {

        private int version = 0;

        private final RuntimeException exception;

        StubArtifactBridge() {
            this.exception = null;
        }

        public ArtifactDescriptor generateArtifactDescriptor(File artifact) throws ArtifactGenerationException {
            if (this.exception == null) {
                return new ArtifactDescriptorBuilder().setUri(artifact.toURI()).setType("dummy").setName("dummy").setVersion(
                    String.valueOf(this.version++)).build();
            }
            throw this.exception;
        }
    }

    private static class StubRepository extends PersistentRepository {

        StubRepository(ArtifactBridge artifactBridge, File indexLocation, EventLogger eventLogger) throws IndexFormatException {
            super(new StubRepositoryConfiguration(artifactBridge, indexLocation), eventLogger);
        }

        public void persist() throws IOException {
            getDepository().persist();
        }

        public void addArtifact(File artifact) throws DuplicateArtifactException {
            RepositoryAwareArtifactDescriptor artifactDescriptor = createArtifactDescriptor(artifact);
            if (artifactDescriptor != null) {
                getDepository().addArtifactDescriptor(artifactDescriptor);
            }
        }

        @Override
        protected RepositoryInfo createMBean() {
            return null;
        }
    }

}
