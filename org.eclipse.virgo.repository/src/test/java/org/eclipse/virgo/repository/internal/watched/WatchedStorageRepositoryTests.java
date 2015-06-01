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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactGenerationException;
import org.eclipse.virgo.repository.IndexFormatException;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.RepositoryCreationException;
import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
import org.eclipse.virgo.repository.builder.AttributeBuilder;
import org.eclipse.virgo.repository.configuration.WatchedStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.LocalRepository;
import org.eclipse.virgo.repository.internal.watched.WatchedStorageRepository;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

/**
 * Test the {@link WatchedStorageRepository} extension of the {@link LocalRepository} class.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe
 * 
 */
public class WatchedStorageRepositoryTests {

    private static final String LOCATIONS_TEST_FILENAME = "a.jar";

    private static final String MBEAN_DOMAIN_VIRGO_WEB_SERVER = "org.eclipse.virgo.server";

    private static MockEventLogger mockEventLogger = new MockEventLogger();

    private static final String ARTIFACT_FILE_NAME = "org.eclipse.virgo.sample.configuration.properties.core.jar";

    private static final String WATCHED_REPO_NAME = "Test-Watched-Repo";

    private static final int WATCH_INTERVAL = 5; // seconds

    private static final int MIN_WATCH_INTERVAL = 1; // seconds

    private static final int LONG_WATCH_INTERVAL = 50; // seconds

    private static final int BAD_WATCH_INTERVAL = 0; // seconds

    private static final File testArtefactFile1 = new File("src/test/resources/artifacts/" + ARTIFACT_FILE_NAME);

    private WatchedStorageRepositoryConfiguration wsrConfiguration;

    private WatchedStorageRepository wsRepository;

    private final File watchDir = new File("build/watchedDir");

    private Set<ArtifactBridge> artefactBridgeDefinitions;

    @Before
    public void setUp() {
        mockEventLogger.reinitialise();
        this.watchDir.mkdirs();
        this.artefactBridgeDefinitions = new HashSet<ArtifactBridge>(4);
        this.artefactBridgeDefinitions.add(new StaticBridge());
        assertTrue("Need jar artifact for tests.", testArtefactFile1.exists());
    }

    @Test
    public void testConstructor() throws RepositoryCreationException, IndexFormatException {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            WATCH_INTERVAL, null);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, new MockEventLogger());
        assertFalse("Didn't create a repository!", null == this.wsRepository);
    }

    @Test
    public void testConstructLongWaiter() throws RepositoryCreationException, IndexFormatException {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            LONG_WATCH_INTERVAL, null);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, mockEventLogger);

        assertFalse("Didn't create a repository!", null == this.wsRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBadWaiter() throws RepositoryCreationException, IndexFormatException {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            BAD_WATCH_INTERVAL, null);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, mockEventLogger);

        assertTrue("Created a repository after exception!", null == this.wsRepository);
    }

    @Test
    public void testGetArtifactLocations() throws Exception {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            WATCH_INTERVAL, null);
        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, mockEventLogger);
        assertFalse("Didn't create a repository!", null == this.wsRepository);

        Set<String> locations = this.wsRepository.getArtifactLocations(LOCATIONS_TEST_FILENAME);
        Set<String> expectedLocations = new HashSet<String>(1);
        expectedLocations.add(new File(this.watchDir,LOCATIONS_TEST_FILENAME).getCanonicalPath());
        assertNotNull("null locations set returned.", locations);
        assertEquals("set of locations not expected", expectedLocations, locations);
    }
    
    @Test
    public void testGetOnEmptyRepository() throws RepositoryCreationException, IOException {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            WATCH_INTERVAL, null);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, mockEventLogger);
        
        assertFalse("Didn't create a repository!", null == this.wsRepository);

        this.wsRepository.start(); // must start it to see things

        VersionRange maxRange = new VersionRange("0");
        ArtifactDescriptor aD = this.wsRepository.get("static", testArtefactFile1.getName(), maxRange);
        assertTrue("A descriptor returned, when it shouldn't!", null == aD);

        this.wsRepository.stop(); // tidy up after one!
    }

    @Test
    public void testGetInitialArtefact() throws RepositoryCreationException, IOException {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            MIN_WATCH_INTERVAL, null);

        putArtifactFile(testArtefactFile1);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, mockEventLogger);
        assertFalse("Didn't create a repository!", null == this.wsRepository);

        this.wsRepository.start(); // must start it to see things

        testDelay(MIN_WATCH_INTERVAL);
        VersionRange maxRange = new VersionRange("0");
        ArtifactDescriptor aD = this.wsRepository.get("static", testArtefactFile1.getName(), maxRange);
        assertTrue("Wrong or null descriptor returned!", (null == aD ? false : aD.getName().equals(testArtefactFile1.getName())));

        this.wsRepository.stop(); // tidy up after one!
    }

    @Test
    public void testGetInitialArtefactWithCheck() throws Exception {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            MIN_WATCH_INTERVAL, null);

        putArtifactFile(testArtefactFile1);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, mockEventLogger);
        assertFalse("Didn't create a repository!", null == this.wsRepository);

        this.wsRepository.start(); // must start it to see things

        this.wsRepository.forceCheck();
        
        VersionRange maxRange = new VersionRange("0");
        ArtifactDescriptor aD = this.wsRepository.get("static", testArtefactFile1.getName(), maxRange);
        assertTrue("Wrong or null descriptor returned!", (null == aD ? false : aD.getName().equals(testArtefactFile1.getName())));

        this.wsRepository.stop(); // tidy up after one!
    }

    /**
     * Short delay (of seconds) in test
     */
    private void testDelay(long secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            // continue
        }
    }

    @Test
    public void testGetAddAndDeleteArtefact() throws RepositoryCreationException, IOException {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            MIN_WATCH_INTERVAL, null);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, new MockEventLogger());
        assertFalse("Didn't create a repository!", null == this.wsRepository);

        this.wsRepository.start(); // must start it to see things

        putArtifactFile(testArtefactFile1);

        testDelay(3 * MIN_WATCH_INTERVAL);

        VersionRange maxRange = new VersionRange("0");
        RepositoryAwareArtifactDescriptor aD = this.wsRepository.get("static", testArtefactFile1.getName(), maxRange);
        assertNotNull("Null descriptor returned.", aD);
        assertEquals(testArtefactFile1.getName(), aD.getName());
        assertEquals("static", aD.getType());
        assertEquals(new Version(0, 1, 0), aD.getVersion());

        removeArtifactFile(testArtefactFile1);

        testDelay(2 * MIN_WATCH_INTERVAL);

        aD = this.wsRepository.get("static", testArtefactFile1.getName(), maxRange);
        assertTrue("A descriptor returned, when it shouldn't!", null == aD);

        this.wsRepository.stop(); // tidy up after one!
    }
    
    @Test
    public void testGetAddAndDeleteArtefactWithCheck() throws RepositoryCreationException, IOException, ArtifactGenerationException, Exception {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            MIN_WATCH_INTERVAL, null);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, new MockEventLogger());
        assertFalse("Didn't create a repository!", null == this.wsRepository);

        this.wsRepository.start(); // watcher needs to be created
        
        putArtifactFile(testArtefactFile1);

        this.wsRepository.forceCheck();

        VersionRange maxRange = new VersionRange("0");
        RepositoryAwareArtifactDescriptor aD = this.wsRepository.get("static", testArtefactFile1.getName(), maxRange);
        assertNotNull("Null descriptor returned.", aD);
        assertEquals(testArtefactFile1.getName(), aD.getName());
        assertEquals("static", aD.getType());
        assertEquals(new Version(0, 1, 0), aD.getVersion());

        removeArtifactFile(testArtefactFile1);

        this.wsRepository.forceCheck();

        aD = this.wsRepository.get("static", testArtefactFile1.getName(), maxRange);
        assertTrue("A descriptor returned, when it shouldn't!", null == aD);

        this.wsRepository.stop(); // tidy up after one!
    }

    @Test
    public void mBeanPublication() throws Exception {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            WATCH_INTERVAL, MBEAN_DOMAIN_VIRGO_WEB_SERVER);

        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, mockEventLogger);
        ObjectName objectName = new ObjectName(MBEAN_DOMAIN_VIRGO_WEB_SERVER + ":type=Repository,name=" + WATCHED_REPO_NAME);
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present until repository has been started");
        } catch (InstanceNotFoundException infe) {
        }

        this.wsRepository.start();

        MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(objectName);
        Object type = mBeanInfo.getDescriptor().getFieldValue("type");
        assertNotNull(type);
        assertEquals("watched", type);

        this.wsRepository.stop();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present once repository has been stopped");
        } catch (InstanceNotFoundException infe) {
        }
    }

    @Test
    public void mBeanNonPublication() throws Exception {
        this.wsrConfiguration = new WatchedStorageRepositoryConfiguration(WATCHED_REPO_NAME, new File("build/watchedIndex"), this.artefactBridgeDefinitions, this.watchDir.getAbsolutePath(),
            WATCH_INTERVAL, null);
        this.wsRepository = new WatchedStorageRepository(this.wsrConfiguration, mockEventLogger);
        ObjectName objectName = new ObjectName(MBEAN_DOMAIN_VIRGO_WEB_SERVER + ":type=Repository,name=" + WATCHED_REPO_NAME);
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present before start");
        } catch (InstanceNotFoundException infe) {
        }

        this.wsRepository.start();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present after start");
        } catch (InstanceNotFoundException infe) {
        }

        this.wsRepository.stop();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present once repository has been stopped");
        } catch (InstanceNotFoundException infe) {
        }
    }

    /**
     * Put artefact file copy into watched directory
     * 
     * @throws IOException
     */
    private File putArtifactFile(File artifactFile) throws IOException {
        File toFile = new File(this.watchDir, artifactFile.getName());
        FileCopyUtils.copy(artifactFile, toFile);
        assertTrue("Failed to copy file into watched directory", toFile.exists());
        return toFile;
    }

    /**
     * Remove artefact file copy from watched directory
     * 
     * @throws IOException
     */
    private void removeArtifactFile(File artifactFile) throws IOException {
        File fileToRemove = new File(this.watchDir, artifactFile.getName());
        fileToRemove.delete();
        assertFalse("Failed to delete file from watched directory", fileToRemove.exists());
    }

    @After
    public void stop() {
        if (null != this.wsRepository) {
            this.wsRepository.stop();
        }
        if (this.watchDir.isDirectory()) {
            if (this.watchDir.exists()) {
                for (File file : this.watchDir.listFiles()) {
                    file.delete();
                }
                this.watchDir.delete();
            }
        }
    }

    /**
     * <p>
     * Implementation of <code>ArtefactBridge</code> that understands how to create a minimal Artefact from any given
     * file. The Artefact name will be that of the file including any extension and the version will always be '0'. The
     * number of bytes in the file, as reported by the operating system will be added as the sole Attribute with a name
     * of size.
     * </p>
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * This class is Threadsafe
     * 
     */
    private static class StaticBridge implements ArtifactBridge {

        public static final String STATIC_ARTIFACT_TYPE = "static";

        public static final String ARTEFACT_ATTRIBUTE_SIZE = "size";

        /**
         * {@inheritDoc}
         */
        public ArtifactDescriptor generateArtifactDescriptor(File artifact) throws ArtifactGenerationException {
            if (!artifact.exists()) {
                throw new ArtifactGenerationException("Artifact must exist at the specified location: " + artifact.getAbsolutePath(),
                    STATIC_ARTIFACT_TYPE);
            }
            ArtifactDescriptorBuilder builder = new ArtifactDescriptorBuilder();
            builder.setUri(artifact.toURI());
            builder.setType(STATIC_ARTIFACT_TYPE);
            builder.setName(artifact.getName());
            builder.setVersion("0.1.0");

            //Exercise the Attribute builder class a bit
            AttributeBuilder attrBuilder = new AttributeBuilder().setName(ARTEFACT_ATTRIBUTE_SIZE);
            attrBuilder.setValue(String.valueOf(artifact.length()));
            attrBuilder.putProperties("key1","value1");
            ArrayList<String> valueList = new ArrayList<String>(1);
            valueList.add("value2");
            attrBuilder.putProperties("key2",valueList);

            builder.addAttribute(attrBuilder.build());

            return builder.build();
        }
    }
}
