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

package org.eclipse.virgo.apps.repository.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.apps.repository.core.internal.ExportingArtifactDescriptorPersister;
import org.eclipse.virgo.apps.repository.core.internal.FilePool;
import org.eclipse.virgo.apps.repository.core.internal.LazyExportableXMLArtifactDescriptorPersister;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.codec.RepositoryCodec;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.internal.StandardArtifactDescriptor;
import org.eclipse.virgo.util.io.PathReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link LazyExportableXMLArtifactDescriptorPersister}
 * <p />
 *
 */
public class LazyExportableXMLArtifactDescriptorPersisterTests {

    private File solePoolFile = new File("build/lazypersistertest/file001");

    private File nextPoolFile = new File("build/lazypersistertest/file002");

    @Test
    public void persistAndLoadArtifacts() throws Exception {

        RepositoryCodec repositoryCodec = new XMLRepositoryCodec();

        new PathReference(solePoolFile).createFile();
        new PathReference(nextPoolFile).createFile();
        FilePool filePool = new StubFilePool(solePoolFile, nextPoolFile);

        ExportingArtifactDescriptorPersister persister = new LazyExportableXMLArtifactDescriptorPersister("repo-name", repositoryCodec, filePool);

        Set<RepositoryAwareArtifactDescriptor> testSet = generateDescriptorSet();

        persister.persistArtifactDescriptors(testSet);

        Set<RepositoryAwareArtifactDescriptor> set = persister.loadArtifacts();

        assertFalse("Sets are identical -- persister should copy them", testSet == set);

        assertEquals("Set of descriptors returned is not the same as the set put in", testSet, set);
    }

    private Set<RepositoryAwareArtifactDescriptor> generateDescriptorSet() {
        Set<RepositoryAwareArtifactDescriptor> setDescriptors = new HashSet<RepositoryAwareArtifactDescriptor>();
        RepositoryAwareArtifactDescriptor raad1 = new StubRepositoryAwareArtifactDescriptor("repo-name",
            new File("build/testlazypersister/raad1").toURI(), "bundle", "bundle.raad1", new Version("1.1"), "raad1", new HashSet<Attribute>());
        RepositoryAwareArtifactDescriptor raad2 = new StubRepositoryAwareArtifactDescriptor("repo-name",
            new File("build/testlazypersister/raad2").toURI(), "bundle", "bundle.raad2", new Version("1.2"), "raad2", new HashSet<Attribute>());

        setDescriptors.add(raad1);
        setDescriptors.add(raad2);

        return setDescriptors;
    }

    @Test
    public void exportIndexFile() throws Exception {
        RepositoryCodec repositoryCodec = new XMLRepositoryCodec();

        new PathReference(solePoolFile).createFile();
        new PathReference(nextPoolFile).createFile();
        FilePool filePool = new StubFilePool(solePoolFile, nextPoolFile);

        ExportingArtifactDescriptorPersister persister = new LazyExportableXMLArtifactDescriptorPersister("repo-name", repositoryCodec, filePool);

        Set<RepositoryAwareArtifactDescriptor> testSet = generateDescriptorSet();
        persister.persistArtifactDescriptors(testSet);

        File exportedFile = persister.exportIndexFile();
        assertNotNull(exportedFile);

        Set<ArtifactDescriptor> decodedOutput = repositoryCodec.read(new FileInputStream(exportedFile));

        assertEquals(removeRepositoryAwareness(testSet), decodedOutput);
    }

    private static Set<ArtifactDescriptor> removeRepositoryAwareness(Set<RepositoryAwareArtifactDescriptor> raSet) {
        final Set<ArtifactDescriptor> result = new HashSet<ArtifactDescriptor>(raSet.size());
        for (ArtifactDescriptor artifactDescriptor : raSet) {
            StandardArtifactDescriptor standardArtifactDescriptor = new StandardArtifactDescriptor(artifactDescriptor.getUri(),
                artifactDescriptor.getType(), artifactDescriptor.getName(), artifactDescriptor.getVersion(), artifactDescriptor.getFilename(),
                artifactDescriptor.getAttributes());
            result.add(standardArtifactDescriptor);
        }
        return result;
    }

    private static class StubRepositoryAwareArtifactDescriptor implements RepositoryAwareArtifactDescriptor {

        private final ArtifactDescriptor artifactDescriptor;

        private final String repositoryName;

        public StubRepositoryAwareArtifactDescriptor(String repositoryName, URI uri, String type, String name, Version version, String filename,
            Set<Attribute> attributes) {
            this.artifactDescriptor = new StandardArtifactDescriptor(uri, type, name, version, filename, attributes);
            this.repositoryName = repositoryName;
        }

        public Set<Attribute> getAttribute(String name) {
            return artifactDescriptor.getAttribute(name);
        }

        public Set<Attribute> getAttributes() {
            return artifactDescriptor.getAttributes();
        }

        public String getFilename() {
            return artifactDescriptor.getFilename();
        }

        public String getName() {
            return artifactDescriptor.getName();
        }

        public String getType() {
            return artifactDescriptor.getType();
        }

        public java.net.URI getUri() {
            return artifactDescriptor.getUri();
        }

        public Version getVersion() {
            return artifactDescriptor.getVersion();
        }

        public String getRepositoryName() {
            return this.repositoryName;
        }
    }

}
