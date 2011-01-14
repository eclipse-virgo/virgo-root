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

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.kernel.core.AbortableSignal;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 */
public class AbstractInstallArtifactTests {

    private static final String PROPERTY_NAME = "name";

    private static final String PROPERTY_VALUE = "value";

    private static final String PROPERTY_VALUE_2 = "value2";

    private TestInstallArtifact installArtifact;

    @Before
    public void setUp() throws Exception {
        installArtifact = new TestInstallArtifact();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetProperty() {
        assertNull(this.installArtifact.getProperty(PROPERTY_NAME));

        this.installArtifact.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
        assertEquals(PROPERTY_VALUE, this.installArtifact.getProperty(PROPERTY_NAME));
    }

    @Test
    public void testGetPropertyNames() {
        assertEquals(0, this.installArtifact.getPropertyNames().size());

        this.installArtifact.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
        Set<String> expectedNames = new HashSet<String>();
        expectedNames.add(PROPERTY_NAME);
        assertEquals(expectedNames, this.installArtifact.getPropertyNames());
    }

    @Test
    public void testSetProperty() {
        assertNull(this.installArtifact.setProperty(PROPERTY_NAME, PROPERTY_VALUE));
        assertEquals(PROPERTY_VALUE, this.installArtifact.setProperty(PROPERTY_NAME, PROPERTY_VALUE_2));
    }

    public static final class TestInstallArtifact extends AbstractInstallArtifact {

        public TestInstallArtifact() {
            super(new ArtifactIdentity("type", "name", Version.emptyVersion, null), new StubArtifactStorage(), null, null, new MockEventLogger());
        }

        @Override
        protected void doStop() throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doUninstall() throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doStart(AbortableSignal signal) throws DeploymentException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected boolean doRefresh() throws DeploymentException {
            return false;
        }
    }

    private static class StubArtifactStorage implements ArtifactStorage {

        public void delete() {
        }

        public ArtifactFS getArtifactFS() {
            return null;
        }

        public void synchronize() {
        }

        public void synchronize(URI sourceUri) {
        }

        public void rollBack() {
        }

    }

}
