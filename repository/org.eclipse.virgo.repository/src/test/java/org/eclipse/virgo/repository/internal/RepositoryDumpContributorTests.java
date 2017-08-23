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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.DuplicateArtifactException;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.repository.codec.XMLRepositoryCodec;
import org.eclipse.virgo.repository.internal.ArtifactDescriptorDepository;
import org.eclipse.virgo.repository.internal.RepositoryDumpContributor;
import org.eclipse.virgo.util.io.PathReference;

/**
 */

public class RepositoryDumpContributorTests {

    private static final String DUMP_TARGET = "build/dump";

    private PathReference target = new PathReference(DUMP_TARGET);

    @Before
    public void before() {
        this.target.delete(true);
    }

    @Test
    public void testDumpEmpty() throws Exception {
        RepositoryDumpContributor contributor = new RepositoryDumpContributor(new XMLRepositoryCodec());
        contributor.contribute(new StubDump());
        assertFalse(target.exists());
    }

    @Test
    public void testDumpWithTwoDepositories() throws Exception {
        RepositoryDumpContributor contributor = new RepositoryDumpContributor(new XMLRepositoryCodec());

        ArtifactDescriptorDepository one = new StubDepository();
        ArtifactDescriptorDepository two = new StubDepository();

        contributor.addDepository("one", one);
        contributor.addDepository("two", two);

        contributor.contribute(new StubDump());
        assertTrue(target.exists());
        assertTrue(this.target.newChild("repository-one.index").exists());
        assertTrue(this.target.newChild("repository-two.index").exists());
    }

    private static final class StubDepository implements ArtifactDescriptorDepository {

        /**
         * {@inheritDoc}
         */
        public void addArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDesc) throws DuplicateArtifactException {
        }

        /**
         * {@inheritDoc}
         */
        public int getArtifactDescriptorCount() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        public void persist() throws IOException {
        }

        /**
         * {@inheritDoc}
         */
        public RepositoryAwareArtifactDescriptor removeArtifactDescriptor(URI uri) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public boolean removeArtifactDescriptor(RepositoryAwareArtifactDescriptor artifactDescriptor) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public Set<RepositoryAwareArtifactDescriptor> resolveArtifactDescriptors(Set<Attribute> filters) {
            RepositoryAwareArtifactDescriptor descriptor = new RepositoryAwareArtifactDescriptor() {

                public Version getVersion() {
                    return Version.emptyVersion;
                }

                public URI getUri() {
                    try {
                        return new URI("file:foo.jar");
                    } catch (URISyntaxException e) {
                        return null;
                    }
                }

                public String getType() {
                    return "bundle";
                }

                public String getRepositoryName() {
                    return "foo";
                }

                public String getName() {
                    return "my artifact";
                }

                public String getFilename() {
                    return "foo.jar";
                }

                public Set<Attribute> getAttributes() {
                    return Collections.<Attribute> emptySet();
                }

                public Set<Attribute> getAttribute(String name) {
                    return getAttributes();
                }
            };
            return Collections.<RepositoryAwareArtifactDescriptor> singleton(descriptor);
        }

    }

    private static final class StubDump implements Dump {

        /**
         * {@inheritDoc}
         */
        public File createFile(String arg0) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public FileOutputStream createFileOutputStream(String name) throws DumpContributionFailedException {
            File file = new File(DUMP_TARGET, name);
            file.getParentFile().mkdirs();
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                throw new DumpContributionFailedException(e.getMessage());
            }
        }

        /**
         * {@inheritDoc}
         */
        public FileWriter createFileWriter(String arg0) throws DumpContributionFailedException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getCause() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public Map<String, Object> getContext() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public Throwable[] getThrowables() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public long getTimestamp() {
            return 0;
        }

    }
}
