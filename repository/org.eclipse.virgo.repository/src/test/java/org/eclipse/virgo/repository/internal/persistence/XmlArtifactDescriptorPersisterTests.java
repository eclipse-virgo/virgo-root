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

package org.eclipse.virgo.repository.internal.persistence;

import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_EIGHT;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ELEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FIFTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FIVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FOUR;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_FOURTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_NINE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_ONE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SEVEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SIX;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_SIXTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_THIRTEEN;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_THREE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TWELVE;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_ARTEFACT_TWO;
import static org.eclipse.virgo.repository.internal.RepositoryTestData.TEST_REPO_ONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.ArtifactDescriptorPersister;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.XmlArtifactDescriptorPersister;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.util.io.FileSystemUtils;

/**
 * <p>
 * Unit tests for {@link XmlArtifactDescriptorPersister}
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe test case
 * 
 */
public class XmlArtifactDescriptorPersisterTests {

    private static Set<RepositoryAwareArtifactDescriptor> ARTEFACTS = new HashSet<RepositoryAwareArtifactDescriptor>();

    private static final String DUMMY_REPOSITORY_NAME = "dummyRepositoryName";

    private final File repositories = new File("build/repositories");

    @Before
    public void setUp() throws Exception {
        FileSystemUtils.deleteRecursively(repositories);

        ARTEFACTS.add(TEST_ARTEFACT_ONE);
        ARTEFACTS.add(TEST_ARTEFACT_TWO);
        ARTEFACTS.add(TEST_ARTEFACT_THREE);
        ARTEFACTS.add(TEST_ARTEFACT_FOUR);
        ARTEFACTS.add(TEST_ARTEFACT_FIVE);
        ARTEFACTS.add(TEST_ARTEFACT_SIX);
        ARTEFACTS.add(TEST_ARTEFACT_SEVEN);
        ARTEFACTS.add(TEST_ARTEFACT_EIGHT);
        ARTEFACTS.add(TEST_ARTEFACT_NINE);
        ARTEFACTS.add(TEST_ARTEFACT_TEN);
        ARTEFACTS.add(TEST_ARTEFACT_ELEVEN);
        ARTEFACTS.add(TEST_ARTEFACT_TWELVE);
        ARTEFACTS.add(TEST_ARTEFACT_THIRTEEN);
        ARTEFACTS.add(TEST_ARTEFACT_FOURTEEN);
        ARTEFACTS.add(TEST_ARTEFACT_FIFTEEN);
        ARTEFACTS.add(TEST_ARTEFACT_SIXTEEN);
    }

    @After
    public void cleanUp() {
        FileSystemUtils.deleteRecursively(repositories);
    }

    @Test
    public void testPersistRepository() throws IOException {
        XmlArtifactDescriptorPersister persistenceHandler = new XmlArtifactDescriptorPersister(new XMLRepositoryCodec(), DUMMY_REPOSITORY_NAME,
            new File(repositories, TEST_REPO_ONE));
        persistenceHandler.persistArtifactDescriptors(ARTEFACTS);
    }

    @Test
    public void testLoadRepository() throws IOException {
        ArtifactDescriptorPersister persistenceHandler = new XmlArtifactDescriptorPersister(new XMLRepositoryCodec(), DUMMY_REPOSITORY_NAME,
            new File(repositories, TEST_REPO_ONE));
        persistenceHandler.persistArtifactDescriptors(ARTEFACTS);
        Set<RepositoryAwareArtifactDescriptor> artefacts2 = persistenceHandler.loadArtifacts();
        assertEquals(ARTEFACTS, artefacts2);
    }

    @Test
    public void roundTripWithAttributeWithEmptyValue() throws IOException {
        File outputFile = new File(repositories, "round-trip");
        assertFalse(outputFile.exists());
        ArtifactDescriptorPersister persister = new XmlArtifactDescriptorPersister(new XMLRepositoryCodec(), DUMMY_REPOSITORY_NAME, outputFile);

        RepositoryAwareArtifactDescriptor descriptor = new RepositoryAwareArtifactDescriptor() {

            public Set<Attribute> getAttribute(String name) {
                return null;
            }

            public Set<Attribute> getAttributes() {
                Set<Attribute> attributes = new HashSet<Attribute>();
                attributes.add(new Attribute() {

                    public String getKey() {
                        return "the-key";
                    }

                    public Map<String, Set<String>> getProperties() {
                        return new HashMap<String, Set<String>>();
                    }

                    public String getValue() {
                        return "";
                    }
                });
                return attributes;
            }

            public String getName() {
                return "bar";
            }

            public String getType() {
                return "foo";
            }

            public java.net.URI getUri() {
                return java.net.URI.create("foo://bar");
            }

            public Version getVersion() {
                return new Version(1, 0, 0);
            }

            public String getFilename() {
                return null;
            }

            public String getRepositoryName() {
                return null;
            }
        };

        persister.persistArtifactDescriptors(new HashSet<RepositoryAwareArtifactDescriptor>(Arrays.asList(descriptor)));

        Set<RepositoryAwareArtifactDescriptor> descriptors = persister.loadArtifacts();
        assertEquals(1, descriptors.size());

        ArtifactDescriptor outputDescriptor = descriptors.iterator().next();
        assertEquals("foo", outputDescriptor.getType());
        assertEquals("bar", outputDescriptor.getName());
        assertEquals(new Version(1, 0, 0), outputDescriptor.getVersion());
        assertEquals(java.net.URI.create("foo://bar"), outputDescriptor.getUri());

        Set<Attribute> attributes = descriptors.iterator().next().getAttributes();
        assertEquals(5, attributes.size());
    }
}
