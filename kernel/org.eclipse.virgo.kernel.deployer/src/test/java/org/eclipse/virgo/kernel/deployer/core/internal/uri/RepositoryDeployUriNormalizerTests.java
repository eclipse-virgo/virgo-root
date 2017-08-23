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

package org.eclipse.virgo.kernel.deployer.core.internal.uri;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.nano.deployer.api.core.DeployerLogEvents;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.internal.uri.RepositoryDeployUriNormaliser;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.test.eventlog.LoggedEvent;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

public class RepositoryDeployUriNormalizerTests {

    private static final String REPO_NAME = "a repo";

    private static final String BUNDLE_VERSION = "6";

    private static final VersionRange BUNDLE_VERSION_RANGE = VersionRange.createExactRange(new Version(BUNDLE_VERSION));

    private static final String BUNDLE_SYMBOLIC_NAME = "foo";

    private static final String BUNDLE_TYPE = "bundle";

    private static final String REPO_URI = "repository:" + BUNDLE_TYPE + "/" + BUNDLE_SYMBOLIC_NAME + "/" + BUNDLE_VERSION;

    private static final URI NORMALISED = URI.create("normalised://normal");

    private final Repository repository = createMock(Repository.class);

    private final MockEventLogger eventLogger = new MockEventLogger();

    private final RepositoryDeployUriNormaliser normaliser = new RepositoryDeployUriNormaliser(repository, eventLogger);

    private final RepositoryAwareArtifactDescriptor artifactDescriptor = new StubArtifactDescriptor();

    @Test
    public void normaliseUriWithVersion() throws DeploymentException {
        URI uri = URI.create("repository:bundle/foo/1.0.0");

        expect(repository.get(BUNDLE_TYPE, BUNDLE_SYMBOLIC_NAME, new VersionRange("[1.0.0,1.0.0]"))).andReturn(artifactDescriptor);

        replay(this.repository);

        URI normalised = this.normaliser.normalise(uri);

        verify(this.repository);

        assertEquals(NORMALISED, normalised);

        assertEquals(0, this.eventLogger.getLoggedEvents().size());
    }

    @Test
    public void normaliseUriWithoutVersion() throws DeploymentException {
        URI uri = URI.create("repository:bundle/foo");

        expect(repository.get(BUNDLE_TYPE, BUNDLE_SYMBOLIC_NAME, VersionRange.NATURAL_NUMBER_RANGE)).andReturn(artifactDescriptor);

        replay(this.repository);

        URI normalised = this.normaliser.normalise(uri);

        verify(this.repository);

        assertEquals(NORMALISED, normalised);

        assertEquals(0, this.eventLogger.getLoggedEvents().size());
    }

    @Test(expected = DeploymentException.class)
    public void normaliseMalformedUri() throws DeploymentException {
        URI uri = URI.create("repository:bundle");

        replay(this.repository);
        URI normalised = null;
        try {
            normalised = this.normaliser.normalise(uri);
        } finally {
            verify(this.repository);

            assertNull(normalised);

            assertEquals(1, this.eventLogger.getLoggedEvents().size());
            assertEquals(uri, this.eventLogger.getLoggedEvents().get(0).getInserts()[0]);
            assertEquals(DeployerLogEvents.REPOSITORY_DEPLOYMENT_URI_MALFORMED.getEventCode(), this.eventLogger.getLoggedEvents().get(0).getCode());
            assertEquals(Level.ERROR, this.eventLogger.getLoggedEvents().get(0).getLevel());
        }
    }

    @Test(expected = DeploymentException.class)
    public void normaliseUriWithInvalidVersion() throws DeploymentException {
        URI uri = URI.create("repository:bundle/foo/6-7");

        replay(this.repository);
        URI normalised = null;
        try {
            normalised = this.normaliser.normalise(uri);
        } finally {
            verify(this.repository);

            assertNull(normalised);

            assertEquals(1, this.eventLogger.getLoggedEvents().size());
            assertEquals("6-7", this.eventLogger.getLoggedEvents().get(0).getInserts()[0]);
            assertEquals(uri, this.eventLogger.getLoggedEvents().get(0).getInserts()[1]);
            assertEquals(DeployerLogEvents.REPOSITORY_DEPLOYMENT_INVALID_VERSION.getEventCode(), this.eventLogger.getLoggedEvents().get(0).getCode());
            assertEquals(Level.ERROR, this.eventLogger.getLoggedEvents().get(0).getLevel());
        }
    }

    @Test(expected = DeploymentException.class)
    public void normaliseUriOfNonExistentArtifact() throws DeploymentException {
        URI uri = URI.create(REPO_URI);

        expect(repository.getName()).andReturn(REPO_NAME);
        expect(repository.get(BUNDLE_TYPE, BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION_RANGE)).andReturn(null);

        replay(this.repository);

        try {
            this.normaliser.normalise(uri);
        } finally {
            List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
            assertEquals(1, loggedEvents.size());
            LoggedEvent loggedEvent = loggedEvents.get(0);
            assertEquals(Level.ERROR, loggedEvent.getLevel());
            assertEquals(DeployerLogEvents.ARTIFACT_NOT_FOUND.getEventCode(), loggedEvent.getCode());
            
            Object[] inserts = loggedEvent.getInserts();
            assertEquals(4, inserts.length);
            
            Object type = inserts[0];
            assertEquals(BUNDLE_TYPE, type);
            
            Object name = inserts[1];
            assertEquals(BUNDLE_SYMBOLIC_NAME, name);
            
            Object versionRange = inserts[2];
            assertEquals(BUNDLE_VERSION_RANGE, versionRange);
            
            Object repoName = inserts[3];
            assertEquals(REPO_NAME, repoName);
        }
    }

    private static final class StubArtifactDescriptor implements RepositoryAwareArtifactDescriptor {

        /**
         * {@inheritDoc}
         */
        public Set<Attribute> getAttribute(String name) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Set<Attribute> getAttributes() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getFilename() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getName() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getRepositoryName() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getType() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public URI getUri() {
            return NORMALISED;
        }

        /**
         * {@inheritDoc}
         */
        public Version getVersion() {
            throw new UnsupportedOperationException();
        }
    }
}
