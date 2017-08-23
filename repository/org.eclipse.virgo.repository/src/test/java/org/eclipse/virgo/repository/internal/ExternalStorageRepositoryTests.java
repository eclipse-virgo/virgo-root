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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.ArtifactBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
import org.eclipse.virgo.repository.internal.external.ExternalStorageRepository;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

public class ExternalStorageRepositoryTests {

    private static final String MBEAN_DOMAIN_VIRGO_WEB_SERVER = "org.eclipse.virgo.server";

    private static final String LOCATIONS_TEST_FILENAME = "a.jar";
    private static final String LOCATIONS_TEST_ROOT = new File(new File(new File("src","test"),"resources"),"locations").getAbsolutePath();
    private static final String LOCATIONS_SEARCH_PATTERN =  LOCATIONS_TEST_ROOT + File.separator + "*" + File.separator + "*.jar";

    
    private final MockEventLogger mockEventLogger = new MockEventLogger();
    
    @Test
    public void mBeanPublication() throws Exception {
        ExternalStorageRepositoryConfiguration configuration = new ExternalStorageRepositoryConfiguration("external-repo", new File("build","index"),
            Collections.<ArtifactBridge> emptySet(), new File(".").getAbsolutePath(), MBEAN_DOMAIN_VIRGO_WEB_SERVER);
        
        mockEventLogger.reinitialise();
        
        ExternalStorageRepository repository = new ExternalStorageRepository(configuration, mockEventLogger);

        assertFalse("Events logged at construction!", mockEventLogger.getCalled());
        
        ObjectName objectName = new ObjectName(MBEAN_DOMAIN_VIRGO_WEB_SERVER + ":type=Repository,name=external-repo");
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present until repository has been started");
        } catch (InstanceNotFoundException infe) {
        }

        repository.start();

        assertFalse("Events logged at start!", mockEventLogger.getCalled());

        MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(objectName);
        Object type = mBeanInfo.getDescriptor().getFieldValue("type");
        assertNotNull(type);
        assertEquals("external", type);

        repository.stop();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present once repository has been stopped");
        } catch (InstanceNotFoundException infe) {
        }
    }
    
    @Test
    public void mBeanNonPublication() throws Exception {
        ExternalStorageRepositoryConfiguration configuration = new ExternalStorageRepositoryConfiguration("external-repo", new File("build","index"),
            Collections.<ArtifactBridge> emptySet(), new File(".").getAbsolutePath(), null);
        
        mockEventLogger.reinitialise();
        
        ExternalStorageRepository repository = new ExternalStorageRepository(configuration, mockEventLogger);

        assertFalse("Events logged at construction!", mockEventLogger.getCalled());
        
        ObjectName objectName = new ObjectName(MBEAN_DOMAIN_VIRGO_WEB_SERVER + ":type=Repository,name=external-repo");
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            platformMBeanServer.getMBeanInfo(objectName);
            fail("MBean should not be present before start");
        } catch (InstanceNotFoundException infe) {
        }

        repository.start();

        assertFalse("Events logged at start!", mockEventLogger.getCalled());
        
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

    @Test
    public void publishAndRetract() throws Exception {
        
        String searchPattern = (new File("build", "nosuchdir").getAbsolutePath() + File.separator + "*");
        
        ArtifactBridge artifactBridge = createMock(ArtifactBridge.class);
        ExternalStorageRepositoryConfiguration configuration = new ExternalStorageRepositoryConfiguration("external-repo", new File("build","index"),
            new HashSet<ArtifactBridge>(Arrays.asList(artifactBridge)), searchPattern, null);

        mockEventLogger.reinitialise();
        

        assertFalse("Events logged at construction!", mockEventLogger.getCalled());

        ArtifactDescriptor artifactDescriptor = createMock(ArtifactDescriptor.class);        
        expect(artifactBridge.generateArtifactDescriptor(eq(new File("/artifact.jar")))).andReturn(artifactDescriptor).anyTimes();
        Set<Attribute> attributes = new HashSet<Attribute>();
        attributes.add(new StubAttribute("type", "bundle"));
        attributes.add(new StubAttribute("name", "foo"));
        attributes.add(new StubAttribute("version", "1.2.3"));

        expect(artifactDescriptor.getAttributes()).andReturn(attributes).anyTimes();
        expect(artifactDescriptor.getVersion()).andReturn(new Version(1, 2, 3)).anyTimes();

        URI artifactUri = URI.create("file:/artifact.jar");
        replay(artifactDescriptor, artifactBridge);

        ExternalStorageRepository repository = new ExternalStorageRepository(configuration, mockEventLogger);

        assertNull(repository.get("bundle", "foo", new VersionRange("[1.0.0,2.0.0)")));
        assertEquals(repository.publish(artifactUri), artifactDescriptor);
        assertEquals(repository.get("bundle", "foo", new VersionRange("[1.0.0,2.0.0)")), artifactDescriptor);

        assertTrue(repository.retract("bundle", "foo", new Version(1, 2, 3)));

        assertNull(repository.get("bundle", "foo", new VersionRange("[1.0.0,2.0.0)")));

        verify(artifactDescriptor, artifactBridge);
        
        assertFalse("Events logged during test!", mockEventLogger.getCalled());
    }

    @Test(expected=IllegalArgumentException.class)
    public void publishBadArtifact() throws Exception {
        ExternalStorageRepositoryConfiguration configuration = new ExternalStorageRepositoryConfiguration("external-repo", new File("build","index"),
            new HashSet<ArtifactBridge>(), new File(".").getAbsolutePath(), null);

        mockEventLogger.reinitialise();
        
        ExternalStorageRepository repository = new ExternalStorageRepository(configuration, mockEventLogger);

        assertFalse("Events logged at construction!", mockEventLogger.getCalled());

        URI artifactUri = URI.create("file:/artifact.jar");
        //Artifact appears bad because there is no Bridge to recognise it.
        
        ArtifactDescriptor artifactDescriptor = repository.publish(artifactUri);
        
        assertNull("Artifact should not be published, but descriptor was returned!", artifactDescriptor);
        
        assertFalse("Events logged during test!", mockEventLogger.getCalled());
    }

    @Test
    public void testGetArtifactLocations() throws Exception {
        ArtifactBridge artifactBridge = createMock(ArtifactBridge.class);
        ExternalStorageRepositoryConfiguration configuration = new ExternalStorageRepositoryConfiguration("external-repo", new File("build","index"),
            new HashSet<ArtifactBridge>(Arrays.asList(artifactBridge)), LOCATIONS_SEARCH_PATTERN, null);

        mockEventLogger.reinitialise();
        
        ExternalStorageRepository repository = new ExternalStorageRepository(configuration, mockEventLogger);

        assertFalse("Events logged at construction!", mockEventLogger.getCalled());

        Set<String> locations = repository.getArtifactLocations(LOCATIONS_TEST_FILENAME);
        Set<String> expectedLocations = new HashSet<String>(1);
        expectedLocations.add(LOCATIONS_TEST_ROOT + File.separator + "dira" + File.separator + LOCATIONS_TEST_FILENAME);
        expectedLocations.add(LOCATIONS_TEST_ROOT + File.separator + "dirb" + File.separator + LOCATIONS_TEST_FILENAME);

        assertNotNull("null locations set returned.", locations);
        assertEquals("set of locations not expected", expectedLocations, locations);
    }
    
    private static class StubAttribute implements Attribute {

        private final String key;

        private final String value;

        private StubAttribute(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Map<String, Set<String>> getProperties() {
            return Collections.<String, Set<String>> emptyMap();
        }

        public String getValue() {
            return value;
        }
    }
}
